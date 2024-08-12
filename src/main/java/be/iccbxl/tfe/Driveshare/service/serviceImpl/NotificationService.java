package be.iccbxl.tfe.Driveshare.service.serviceImpl;

import be.iccbxl.tfe.Driveshare.DTO.MapperDTO;
import be.iccbxl.tfe.Driveshare.DTO.NotificationDTO;
import be.iccbxl.tfe.Driveshare.DTO.ReservationDTO;
import be.iccbxl.tfe.Driveshare.model.Car;
import be.iccbxl.tfe.Driveshare.model.Notification;
import be.iccbxl.tfe.Driveshare.model.Reservation;
import be.iccbxl.tfe.Driveshare.model.User;
import be.iccbxl.tfe.Driveshare.repository.NotificationRepository;
import be.iccbxl.tfe.Driveshare.service.NotificationServiceI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class NotificationService implements NotificationServiceI {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserService userService;
    @Autowired
    private CarService carService;

    @Autowired
    private ReservationService reservationService;

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


    public List<NotificationDTO> getNotificationsByUser(Long userId) {
        return notificationRepository.findByToUserId(userId)
                .stream()
                .map(MapperDTO::toNotificationDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<Notification> getAllNotifications(User user) {
        // Vous pouvez ajuster cette méthode selon vos besoins spécifiques
        return notificationRepository.findByToUserOrFromUser(user, user);
    }
    public List<NotificationDTO> getAllNotifications() {
        List<Notification> notifications = notificationRepository.findAll();
        return notifications.stream().map(MapperDTO::toNotificationDTO).collect(Collectors.toList());
    }
    @Override
    public List<Notification> getReceivedNotifications(User user) {
        return notificationRepository.findByToUser(user);
    }

    @Override
    public List<Notification> getSentNotifications(User user) {
        return notificationRepository.findByFromUser(user);
    }

    @Override
    public List<Notification> getNotifications(User user) {
        // Cette méthode peut être personnalisée pour des notifications spécifiques
        return notificationRepository.findByToUser(user);
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


    public String renderNotificationsHtml(List<Notification> notifications, User currentUser) {
        StringBuilder html = new StringBuilder();

        for (Notification notification : notifications) {
            boolean isReceived = notification.getToUser().equals(currentUser);

            html.append("<div class='card ")
                    .append(notification.isLu() ? "read" : "unread")
                    .append(" mb-3'>")
                    .append("<div class='card-body'>");

            if (isReceived) {
                // Pour les messages reçus
                html.append("<div class='d-flex align-items-center mb-3'>")
                        .append("<img src='/uploads/profil/")
                        .append(notification.getFromUser().getPhotoUrl())
                        .append("' alt='Profile' class='rounded-circle me-2' style='width: 40px; height: 40px;'>")
                        .append("<div>")
                        .append("<h5 class='card-title mb-0'>")
                        .append(notification.getFromUser().getPrenom()).append(" ").append(notification.getFromUser().getNom())
                        .append("</h5>")
                        .append("<small class='text-muted'>").append(notification.getDateEnvoi()).append("</small>")
                        .append("</div>")
                        .append("</div>")
                        .append("<p class='card-text'>").append(notification.getMessage()).append("</p>")
                        .append("<button class='btn btn-primary mt-2' data-bs-toggle='modal' data-bs-target='#replyModal")
                        .append(notification.getId()).append("'>Répondre</button>");
            } else {
                // Pour les messages envoyés
                html.append("<div class='d-flex align-items-center mb-3'>")
                        .append("<img src='/uploads/profil/")
                        .append(notification.getToUser().getPhotoUrl())
                        .append("' alt='Profile' class='rounded-circle me-2' style='width: 40px; height: 40px;'>")
                        .append("<div>")
                        .append("<h5 class='card-title mb-0'>")
                        .append(notification.getToUser().getPrenom()).append(" ").append(notification.getToUser().getNom())
                        .append("</h5>")
                        .append("<small class='text-muted'>").append(notification.getDateEnvoi()).append("</small>")
                        .append("</div>")
                        .append("</div>")
                        .append("<p class='card-text'>").append(notification.getMessage()).append("</p>");
            }

            html.append("</div>") // card-body
                    .append("</div>"); // card
        }

        return html.toString();
    }


    public List<NotificationDTO> createComplaintNotification(NotificationDTO notificationDTO) {
        List<User> admins = userService.findByRole("ADMIN");

        if (admins.isEmpty()) {
            throw new RuntimeException("Aucun administrateur trouvé");
        }

        Reservation reservation = reservationService.getReservationById(notificationDTO.getReservationId());
        if (reservation == null) {
            throw new RuntimeException("Réservation non trouvée pour l'ID : " + notificationDTO.getReservationId());
        }

        Car car = reservation.getCar();
        if (car == null) {
            throw new RuntimeException("Aucune voiture associée à cette réservation");
        }

        Set<Long> processedAdmins = new HashSet<>(); // Utiliser un Set pour stocker les IDs d'administrateurs déjà traités

        List<NotificationDTO> notifications = new ArrayList<>();
        for (User admin : admins) {
            if (!processedAdmins.contains(admin.getId())) { // Vérifiez si cet administrateur a déjà reçu la notification
                Notification notification = new Notification();
                notification.setFromUser(userService.getUserById(notificationDTO.getFromUserId()));
                notification.setToUser(admin);
                notification.setCar(car);
                notification.setType("Réclamation");
                notification.setMessage(notificationDTO.getMessage());
                notification.setDateEnvoi(LocalDateTime.now());
                notification.setLu(false);

                // Convertir l'entité Notification en DTO
                Notification savedNotification = notificationRepository.save(notification);
                NotificationDTO notificationDTOResult = convertToDTO(savedNotification, reservation.getId());

                notifications.add(notificationDTOResult);

                processedAdmins.add(admin.getId()); // Ajoutez l'administrateur à l'ensemble des admins traités
            }
        }

        return notifications;
    }



    private NotificationDTO convertToDTO(Notification notification, Long reservationId) {
        NotificationDTO dto = new NotificationDTO();
        dto.setMessage(notification.getMessage());
        dto.setFromUserId(notification.getFromUser().getId());
        dto.setToUserId(notification.getToUser().getId());
        dto.setCarId(notification.getCar().getId());
        dto.setType(notification.getType());
        dto.setLu(notification.isLu());
        dto.setReservationId(reservationId); // Ajoutez l'ID de la réservation au DTO
        return dto;
    }







}
