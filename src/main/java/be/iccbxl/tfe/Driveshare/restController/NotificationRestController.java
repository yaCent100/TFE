package be.iccbxl.tfe.Driveshare.restController;

import be.iccbxl.tfe.Driveshare.DTO.MapperDTO;
import be.iccbxl.tfe.Driveshare.DTO.NotificationDTO;
import be.iccbxl.tfe.Driveshare.model.Car;
import be.iccbxl.tfe.Driveshare.model.Claim;
import be.iccbxl.tfe.Driveshare.model.Notification;
import be.iccbxl.tfe.Driveshare.model.User;
import be.iccbxl.tfe.Driveshare.service.serviceImpl.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@Tag(name = "Notification API", description = "Gestion des notifications")
public class NotificationRestController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserService userService;

    @Autowired
    private CarService carService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private ClaimService claimService;

    @Operation(summary = "Envoyer une notification", description = "Permet d'envoyer une notification à l'utilisateur.")
    @MessageMapping("/notification/carId/{carId}")
    @SendTo("/topic/notifications/{carId}")
    public NotificationDTO sendNotification(
            @RequestBody NotificationDTO notificationDTO,
            @DestinationVariable Long carId,
            Principal principal) {
        String email = principal.getName();
        User fromUser = userService.findByEmail(email);
        User toUser = userService.getUserById(notificationDTO.getToUserId());
        Car vehicle = carService.getCarById(carId);

        Notification notification = new Notification();
        notification.setFromUser(fromUser);
        notification.setToUser(toUser);
        notification.setCar(vehicle);
        notification.setMessage(notificationDTO.getMessage());
        notification.setType(notificationDTO.getType());
        notification.setSentAt(LocalDateTime.now());

        Notification savedNotification = notificationService.save(notification);

        emailService.sendNotificationEmail(toUser.getEmail(), "Nouvelle notification", notificationDTO.getMessage());

        return MapperDTO.toNotificationDTO(savedNotification);
    }

    @Operation(summary = "Filtrer les notifications", description = "Retourne les notifications en fonction des filtres appliqués.")
    @GetMapping("/api/notifications/filter")
    @ResponseBody
    public String filterNotifications(
            @RequestParam boolean nouveauMessages,
            @RequestParam boolean recuMessages,
            @RequestParam boolean envoyeMessages,
            Principal principal) {

        String email = principal.getName();
        User currentUser = userService.findByEmail(email);
        List<Notification> filteredNotifications;

        if (nouveauMessages) {
            filteredNotifications = notificationService.getUnreadNotifications(currentUser);
        } else if (recuMessages) {
            filteredNotifications = notificationService.getReceivedNotifications(currentUser);
        } else if (envoyeMessages) {
            filteredNotifications = notificationService.getSentNotifications(currentUser);
        } else {
            filteredNotifications = Collections.emptyList(); // Aucun filtre sélectionné
        }

        return notificationService.renderNotificationsHtml(filteredNotifications, currentUser);
    }



    // API pour répondre à une notification
    @PostMapping("/api/notifications/reply/{notificationId}")
    public ResponseEntity<Map<String, String>> replyToNotification(
            @PathVariable Long notificationId,
            @RequestBody Map<String, String> payload,
            Principal principal) {

        String replyMessage = payload.get("replyMessage");

        // Log pour vérifier si le message est bien reçu
        System.out.println("Réponse reçue: " + replyMessage);

        if (replyMessage == null || replyMessage.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Collections.singletonMap("error", "Le message de réponse est vide."));
        }

        // Récupérer l'utilisateur authentifié
        String email = principal.getName();
        User currentUser = userService.findByEmail(email);

        // Récupérer la notification par son ID
        Notification notification = notificationService.getNotificationById(notificationId);
        if (notification == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Collections.singletonMap("error", "Notification introuvable"));
        }

        // Marquer la notification comme lue
        notification.setIsRead(true);
        notificationService.save(notification);

        // Traitez la réponse (enregistrer la nouvelle notification de réponse)
        Notification replyNotification = new Notification();
        replyNotification.setFromUser(currentUser);
        replyNotification.setToUser(notification.getFromUser()); // Envoyer la réponse à l'expéditeur d'origine
        replyNotification.setCar(notification.getCar());
        replyNotification.setMessage(replyMessage); // Enregistrer la réponse
        replyNotification.setType("Réponse");
        replyNotification.setSentAt(LocalDateTime.now());
        notificationService.save(replyNotification);

        // Retourner une réponse JSON valide
        return ResponseEntity.ok(Collections.singletonMap("message", "Réponse envoyée avec succès"));
    }





    @Operation(summary = "Envoyer une plainte", description = "Envoie une notification de plainte à l'utilisateur concerné.")
    @PostMapping("/api/notifications/complaint")
    public ResponseEntity<List<NotificationDTO>> sendComplaintNotification(
            @RequestBody NotificationDTO notificationDTO) {
        List<NotificationDTO> notifications = notificationService.createComplaintNotification(notificationDTO);
        return ResponseEntity.ok(notifications);
    }
}



