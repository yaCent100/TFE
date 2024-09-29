package be.iccbxl.tfe.Driveshare.controller;

import be.iccbxl.tfe.Driveshare.DTO.ChatMessageDTO;
import be.iccbxl.tfe.Driveshare.DTO.MapperDTO;
import be.iccbxl.tfe.Driveshare.model.ChatMessage;
import be.iccbxl.tfe.Driveshare.model.Reservation;
import be.iccbxl.tfe.Driveshare.model.User;
import be.iccbxl.tfe.Driveshare.security.CustomUserDetail;
import be.iccbxl.tfe.Driveshare.service.serviceImpl.ChatMessageService;
import be.iccbxl.tfe.Driveshare.service.serviceImpl.EmailService;
import be.iccbxl.tfe.Driveshare.service.serviceImpl.ReservationService;
import be.iccbxl.tfe.Driveshare.service.serviceImpl.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.time.LocalDateTime;

@Controller
public class ChatController {

    private final ChatMessageService chatMessageService;
    private final UserService userService;

    private final ReservationService reservationService;

    private final EmailService emailService;  // Service d'envoi d'emails
    private final Logger logger = LoggerFactory.getLogger(ChatController.class);

    @Autowired
    public ChatController(ChatMessageService chatMessageService, UserService userService, ReservationService reservationService, EmailService emailService) {
        this.chatMessageService = chatMessageService;
        this.userService = userService;
        this.reservationService = reservationService;
        this.emailService = emailService;
    }


    @MessageMapping("/chat/{reservationId}")
    @SendTo("/topic/messages/{reservationId}")
    public ChatMessageDTO sendMessage(ChatMessageDTO chatMessageDTO,
                                      @DestinationVariable Long reservationId,
                                      Principal principal) {
        // Vérification de l'authentification
        if (principal == null) {
            throw new RuntimeException("No authentication available");
        }

        // Récupération de l'utilisateur courant à partir du Principal
        CustomUserDetail userDetails = (CustomUserDetail) ((UsernamePasswordAuthenticationToken) principal).getPrincipal();
        Long currentUserId = userDetails.getUser().getId();

        // Récupération de la réservation en fonction de l'ID de réservation
        Reservation reservation = reservationService.getReservationById(reservationId);
        if (reservation == null) {
            throw new RuntimeException("Reservation not found");
        }

        // Récupération des utilisateurs liés à la réservation
        Long reservationUserId = reservation.getUser().getId();  // Le locataire
        Long carOwnerId = reservation.getCar().getUser().getId();  // Le propriétaire de la voiture

        // Déterminer l'expéditeur et le destinataire en fonction de l'utilisateur courant
        Long toUserId;
        if (currentUserId.equals(reservationUserId)) {
            // Si l'utilisateur actuel est le locataire
            chatMessageDTO.setFromUserId(reservationUserId);
            chatMessageDTO.setToUserId(carOwnerId);
            toUserId = carOwnerId;  // Le propriétaire de la voiture est le destinataire
        } else if (currentUserId.equals(carOwnerId)) {
            // Si l'utilisateur actuel est le propriétaire de la voiture
            chatMessageDTO.setFromUserId(carOwnerId);
            chatMessageDTO.setToUserId(reservationUserId);
            toUserId = reservationUserId;  // Le locataire est le destinataire
        } else {
            throw new RuntimeException("User not part of the reservation");
        }

        // Création du message de chat
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setContent(chatMessageDTO.getContent());
        chatMessage.setReservation(reservation);
        chatMessage.setFromUserId(chatMessageDTO.getFromUserId());
        chatMessage.setToUserId(chatMessageDTO.getToUserId());
        chatMessage.setSentAt(LocalDateTime.now());

        // Sauvegarde du message dans la base de données
        ChatMessage savedMessage = chatMessageService.save(chatMessage);

        // Envoyer un email au destinataire pour l'informer du message reçu
        // Récupérer le destinataire
        User toUser = userService.getUserById(toUserId);
        if (toUser != null && toUser.getEmail() != null) {
            String subject = "Message Chat Réservation n°" + reservation.getId();  // Sujet personnalisé
            String messageContent = "Bonjour,\n\n" +
                    "Vous avez reçu un nouveau message dans le chat concernant la réservation n°" + reservation.getId() + ".\n" +
                    "Détail du message : \n\n\"" + chatMessage.getContent() + "\"\n\n" +
                    "Veuillez vous connecter pour répondre.\n\n" +
                    "Cordialement,\n" +
                    "L'équipe DriveShare";  // Mention équipe DriveShare

            // Appel au service d'envoi d'email avec le nouveau contenu personnalisé
            emailService.sendNotificationEmail(toUser.getEmail(), subject, messageContent);
        }


        // Retourne le DTO du message sauvé pour la diffusion via WebSocket
        return MapperDTO.toChatMessageDTO(savedMessage, currentUserId);
    }







}




