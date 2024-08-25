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
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

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
            @RequestParam boolean tousMessages,
            @RequestParam boolean recuMessages,
            @RequestParam boolean envoyeMessages,
            @RequestParam boolean notifications,
            Principal principal) {

        String email = principal.getName();
        User currentUser = userService.findByEmail(email);
        List<Notification> filteredNotifications;

        // Filtrer les notifications en fonction des cases cochées
        if (tousMessages) {
            filteredNotifications = notificationService.getAllNotifications(currentUser);
        } else if (recuMessages) {
            filteredNotifications = notificationService.getReceivedNotifications(currentUser);
        } else if (envoyeMessages) {
            filteredNotifications = notificationService.getSentNotifications(currentUser);
        } else if (notifications) {
            filteredNotifications = notificationService.getNotifications(currentUser);
        } else {
            filteredNotifications = Collections.emptyList();
        }

        return notificationService.renderNotificationsHtml(filteredNotifications, currentUser);
    }





    @Operation(summary = "Envoyer une plainte", description = "Envoie une notification de plainte à l'utilisateur concerné.")
    @PostMapping("/api/notifications/complaint")
    public ResponseEntity<List<NotificationDTO>> sendComplaintNotification(
            @RequestBody NotificationDTO notificationDTO) {
        List<NotificationDTO> notifications = notificationService.createComplaintNotification(notificationDTO);
        return ResponseEntity.ok(notifications);
    }
}



