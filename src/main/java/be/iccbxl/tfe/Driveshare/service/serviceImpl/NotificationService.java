package be.iccbxl.tfe.Driveshare.service.serviceImpl;

import be.iccbxl.tfe.Driveshare.model.Notification;
import be.iccbxl.tfe.Driveshare.model.User;
import be.iccbxl.tfe.Driveshare.repository.NotificationRepository;
import be.iccbxl.tfe.Driveshare.service.NotificationServiceI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class NotificationService implements NotificationServiceI {

    @Autowired
    private NotificationRepository notificationRepository;

    public Notification save(Notification notification) {
        return notificationRepository.save(notification);
    }



    @Override
    public List<Notification> getNotificationsForCar(Long carId) {
        return notificationRepository.findByCarId(carId);
    }

    @Override
    public List<Notification> getNotificationsForUser(Long userId) {
        return notificationRepository.findByToUserId(userId);
    }


    public Notification getNotificationById(Long notificationId) {
        return notificationRepository.findNotificationById(notificationId);
    }


    public List<Notification> filterNotifications(boolean nouveauxMessages, boolean tousMessages, boolean notifications) {
        // Implémentez la logique pour filtrer les notifications en fonction des paramètres
        if (nouveauxMessages) {
            return getUnreadNotifications();
        } else if (tousMessages) {
            return notificationRepository.findAll();
        } else if (notifications) {
            // Suppose that `Notification` has a type or category indicating it's a notification.
            return notificationRepository.findByType("notification");
        } else {
            return List.of(); // Return an empty list if no filters are selected
        }
    }


    public List<Notification> getUnreadNotifications() {
        // Implémentez la logique pour récupérer les notifications non lues
        return notificationRepository.findByLuFalse();
    }

    public List<Notification> getReadNotifications() {
        // Implémentez la logique pour récupérer les notifications lues
        return notificationRepository.findByLuTrue();
    }





}
