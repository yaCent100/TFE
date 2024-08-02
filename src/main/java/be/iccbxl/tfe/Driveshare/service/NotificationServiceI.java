package be.iccbxl.tfe.Driveshare.service;

import be.iccbxl.tfe.Driveshare.model.Notification;

import java.util.List;
import java.util.Optional;

public interface NotificationServiceI {

    List<Notification> getNotificationsForCar(Long car);

    List<Notification> getNotificationsForUser(Long userId);
}
