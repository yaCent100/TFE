package be.iccbxl.tfe.Driveshare.repository;

import be.iccbxl.tfe.Driveshare.model.Notification;
import be.iccbxl.tfe.Driveshare.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByCarId(Long carId);

    List<Notification> findByToUserId(Long userId);
    Notification findNotificationById(Long notificationId);

    List<Notification> findByType(String notification);

    List<Notification> findByLuFalse();

    List<Notification> findByLuTrue();

    List<Notification> findByToUser(User user);
    List<Notification> findByFromUser(User user);
    List<Notification> findByToUserOrFromUser(User toUser, User fromUser);

}
