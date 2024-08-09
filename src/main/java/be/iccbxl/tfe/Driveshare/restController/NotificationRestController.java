package be.iccbxl.tfe.Driveshare.restController;

import be.iccbxl.tfe.Driveshare.DTO.MapperDTO;
import be.iccbxl.tfe.Driveshare.DTO.NotificationDTO;
import be.iccbxl.tfe.Driveshare.model.Car;
import be.iccbxl.tfe.Driveshare.model.Notification;
import be.iccbxl.tfe.Driveshare.model.User;
import be.iccbxl.tfe.Driveshare.service.serviceImpl.CarService;
import be.iccbxl.tfe.Driveshare.service.serviceImpl.EmailService;
import be.iccbxl.tfe.Driveshare.service.serviceImpl.NotificationService;
import be.iccbxl.tfe.Driveshare.service.serviceImpl.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@RestController
public class NotificationRestController {

    @Autowired
    private NotificationService notificationService;
    @Autowired
    private UserService userService;
    @Autowired
    private CarService carService;

    @Autowired
    private EmailService emailService;

    @MessageMapping("/notification/carId/{carId}")
    @SendTo("/topic/notifications/{carId}")
    public NotificationDTO sendNotification(NotificationDTO notificationDTO, @DestinationVariable Long carId, Principal principal) {
        if (notificationDTO.getToUserId() == null) {
            throw new RuntimeException("toUserId is null");
        }

        // Obtenir l'utilisateur à partir du principal
        String email = principal.getName(); // Suppose que l'email est utilisé comme nom principal
        User fromUser = userService.findByEmail(email);
        User toUser = userService.getUserById(notificationDTO.getToUserId());
        Car vehicle = carService.getCarById(carId);

        // Créer une nouvelle notification
        Notification notification = new Notification();
        notification.setFromUser(fromUser);
        notification.setToUser(toUser);
        notification.setCar(vehicle);
        notification.setMessage(notificationDTO.getMessage());
        notification.setType(notificationDTO.getType());
        notification.setLu(false);
        notification.setDateEnvoi(LocalDateTime.now());

        // Sauvegarder la notification
        Notification savedNotification = notificationService.save(notification);

        // Send email notification
        String subject = "Vous avez reçu une nouvelle notification";
        String body = "Bonjour " + toUser.getPrenom() + ",\n\nVous avez reçu une nouvelle notification de " + fromUser.getPrenom() + " " + fromUser.getNom() + ".\n\nMessage: " + notificationDTO.getMessage() + "\n\nCordialement,\nL'équipe Driveshare";
        emailService.sendNotificationEmail(toUser.getEmail(), subject, body);


        // Convertir la notification en DTO pour l'envoyer au client
        return MapperDTO.toNotificationDTO(savedNotification);
    }


    @GetMapping("/api/notifications/filter")
    @ResponseBody
    public String filterNotifications(@RequestParam boolean tousMessages,
                                      @RequestParam boolean recuMessages,
                                      @RequestParam boolean envoyeMessages,
                                      @RequestParam boolean notifications,
                                      Principal principal) {
        String email = principal.getName();
        User currentUser = userService.findByEmail(email);

        List<Notification> filteredNotifications;

        if (tousMessages) {
            filteredNotifications = notificationService.getAllNotifications(currentUser);
        } else if (recuMessages) {
            filteredNotifications = notificationService.getReceivedNotifications(currentUser);
        } else if (envoyeMessages) {
            filteredNotifications = notificationService.getSentNotifications(currentUser);
        } else if (notifications) {
            filteredNotifications = notificationService.getNotifications(currentUser);
        } else {
            filteredNotifications = Collections.emptyList(); // Cas par défaut si aucun filtre n'est sélectionné
        }

        return notificationService.renderNotificationsHtml(filteredNotifications, currentUser);
    }


}



