package be.iccbxl.tfe.Driveshare.restController;

import be.iccbxl.tfe.Driveshare.DTO.CarMapper;
import be.iccbxl.tfe.Driveshare.DTO.NotificationDTO;
import be.iccbxl.tfe.Driveshare.model.Car;
import be.iccbxl.tfe.Driveshare.model.Notification;
import be.iccbxl.tfe.Driveshare.model.User;
import be.iccbxl.tfe.Driveshare.service.serviceImpl.CarService;
import be.iccbxl.tfe.Driveshare.service.serviceImpl.EmailService;
import be.iccbxl.tfe.Driveshare.service.serviceImpl.NotificationService;
import be.iccbxl.tfe.Driveshare.service.serviceImpl.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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
        return CarMapper.toDTO(savedNotification);
    }


    @GetMapping("/api/notifications/car/{carId}")
    @ResponseBody
    public List<Notification> getNotificationsForVehicle(@PathVariable Long vehicleId) {
        return notificationService.getNotificationsForCar(vehicleId);
    }


}
