package be.iccbxl.tfe.Driveshare.controller;

import be.iccbxl.tfe.Driveshare.DTO.CarMapper;
import be.iccbxl.tfe.Driveshare.DTO.ChatMessageDTO;
import be.iccbxl.tfe.Driveshare.model.ChatMessage;
import be.iccbxl.tfe.Driveshare.model.Reservation;
import be.iccbxl.tfe.Driveshare.model.User;
import be.iccbxl.tfe.Driveshare.security.CustomUserDetail;
import be.iccbxl.tfe.Driveshare.service.serviceImpl.ChatMessageService;
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
    private final Logger logger = LoggerFactory.getLogger(ChatController.class);

    @Autowired
    public ChatController(ChatMessageService chatMessageService, UserService userService, ReservationService reservationService) {
        this.chatMessageService = chatMessageService;
        this.userService = userService;
        this.reservationService = reservationService;
    }


    @MessageMapping("/chat/{reservationId}")
    @SendTo("/topic/messages/{reservationId}")
    public ChatMessageDTO sendMessage(
            ChatMessageDTO chatMessageDTO,
            @DestinationVariable Long reservationId,
            Principal principal) {

        // Vérification de l'authentification
        if (principal == null) {
            throw new RuntimeException("No authentication available");
        }

        // Récupération de l'utilisateur courant
        CustomUserDetail userDetails = (CustomUserDetail) ((UsernamePasswordAuthenticationToken) principal).getPrincipal();
        User fromUser = userService.getUserById(chatMessageDTO.getFromUserId());
        if (fromUser == null) {
            throw new RuntimeException("FromUser not found");
        }

        User toUser = userService.getUserById(chatMessageDTO.getToUserId());
        if (toUser == null) {
            throw new RuntimeException("ToUser not found");
        }

        // Récupération de la réservation
        Reservation reservation = reservationService.getReservationById(reservationId);
        if (reservation == null) {
            throw new RuntimeException("Reservation not found");
        }

        // Création du message de chat
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setContent(chatMessageDTO.getContent());
        chatMessage.setReservation(reservation);
        chatMessage.setSentAt(LocalDateTime.now());

        // Sauvegarde du message dans la base de données
        ChatMessage savedMessage = chatMessageService.save(chatMessage);

        // Conversion de l'entité en DTO pour la réponse
        return CarMapper.toChatMessageDTO(savedMessage);
    }



}




