package be.iccbxl.tfe.Driveshare.service;

import be.iccbxl.tfe.Driveshare.model.Notification;
import be.iccbxl.tfe.Driveshare.model.Reservation;
import be.iccbxl.tfe.Driveshare.model.User;

import java.util.List;
import java.util.Optional;

public interface NotificationServiceI {

    List<Notification> getNotificationsForCar(Long car);

    List<Notification> getNotificationsForUser(Long userId);

    List<Notification> getAllNotifications(User user);
    List<Notification> getReceivedNotifications(User user);
    List<Notification> getSentNotifications(User user);
    List<Notification> getNotifications(User user);

}
