package be.iccbxl.tfe.Driveshare.service.serviceImpl;

import be.iccbxl.tfe.Driveshare.DTO.MapperDTO;
import be.iccbxl.tfe.Driveshare.DTO.NotificationDTO;
import be.iccbxl.tfe.Driveshare.model.*;
import be.iccbxl.tfe.Driveshare.repository.NotificationRepository;
import be.iccbxl.tfe.Driveshare.service.NotificationServiceI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
        if (nouveauxMessages) {
            // Retourner uniquement les notifications non lues
            return notificationRepository.findByIsReadFalse();
        } else if (tousMessages) {
            // Retourner toutes les notifications
            return notificationRepository.findAll();
        } else if (notifications) {
            // Retourner les notifications qui ont un type spécifique, supposons "notification"
            return notificationRepository.findByType("notification");
        } else {
            // Si aucun filtre n'est sélectionné, retourner une liste vide
            return List.of();
        }
    }



    public List<Notification> getUnreadNotifications(User currentUser) {
        // Récupère toutes les notifications non lues et filtre celles qui sont destinées à l'utilisateur actuel
        return notificationRepository.findByIsReadFalse().stream()
                .filter(notification -> notification.getToUser().equals(currentUser))
                .collect(Collectors.toList());
    }

    public List<Notification> getReadNotifications() {
        // Implémentez la logique pour récupérer les notifications lues
        return notificationRepository.findByIsReadTrue();
    }




    public String renderNotificationsHtml(List<Notification> notifications, User currentUser) {
        StringBuilder html = new StringBuilder();

        // Si aucune notification n'est trouvée
        if (notifications.isEmpty()) {
            // Ajouter un message d'information avec une image de notification barrée
            html.append("<div class='d-flex flex-column align-items-center justify-content-center' style='min-height: 200px;'>")
                    .append("<img src='/icons/no-notifications.png' alt='Pas de notifications' class='img-fluid' style='max-width: 150px;'>")
                    .append("<h5 class='mt-3 text-muted'>Aucune notification trouvée</h5>")
                    .append("</div>");
            return html.toString();  // Retourner ici car il n'y a pas de notifications à afficher
        }

        // Formatter pour la date au format "dd/MM/yyyy 'à' HH'h'mm"
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy 'à' HH'h'mm");

        for (Notification notification : notifications) {
            boolean isReceived = notification.getToUser().equals(currentUser);
            Car car = notification.getCar(); // Récupérer la voiture liée à la notification
            User fromUser = notification.getFromUser(); // Utilisateur qui a envoyé la notification

            // Carte de notification
            html.append("<div class='card card-reservation-profil mb-3'>")
                    .append("<div class='row g-0'>");

            // Colonne principale (col-lg-9) avec image, marque de voiture, expéditeur, message
            html.append("<div class='col-lg-9'><div class='card-body'>")
                    .append("<div class='d-flex align-items-center'>");

            // Image de la voiture en petit, format "rounded-circle"
            html.append("<img src='/uploads/photo-car/")
                    .append(car.getPhotos().isEmpty() ? "default-car.jpg" : car.getPhotos().get(0).getUrl()) // Image de la voiture ou image par défaut
                    .append("' class='img-fluid rounded-circle' alt='").append(car.getBrand()).append(" ").append(car.getModel()).append("' style='width: 50px; height: 50px; margin-right: 10px;'>");

            // Infos sur la voiture et l'utilisateur qui envoie
            html.append("<div>")
                    .append("<span class='fw-bold'>").append(car.getBrand()).append(" ").append(car.getModel()).append("</span><br>")
                    .append("<span class='text-muted fs-13'>Envoyé par : ").append(fromUser.getFirstName()).append(" ").append(fromUser.getLastName()).append("</span>")
                    .append("</div>")
                    .append("</div>") // Fin du d-flex align-items-center

                    // Message de la notification
                    .append("<div class='mt-2'>")
                    .append("<span class='card-text fs-14'>").append(notification.getMessage()).append("</span>")
                    .append("</div>")
                    .append("</div></div>"); // Fin du col-lg-9

            // Colonne date + bouton (col-lg-3)
            html.append("<div class='col-lg-3 d-flex flex-column justify-content-between text-end'>");

            // Formater la date de la notification
            String formattedDate = notification.getSentAt().format(formatter);

            // Date en haut
            html.append("<div>")
                    .append("<span class='text-muted fs-13'>Envoyé le: ").append(formattedDate).append("</span>")
                    .append("</div>");

            // Si le message a été lu, afficher "Message lu". Sinon, afficher le bouton "Répondre"
            if (notification.getIsRead()) {
                html.append("<div class='mt-2'>")
                        .append("<span class='text-muted fs-13'>Message lu</span>")
                        .append("</div>");
            } else {
                html.append("<div class='mt-2'>")
                        .append("<button class='btn btn-primary' data-bs-toggle='modal' data-bs-target='#replyModal")
                        .append(notification.getId()).append("'>Répondre</button>")
                        .append("</div>");
            }

            html.append("</div></div></div>"); // Fin du col-lg-3 et de la carte

            // Modal HTML pour la réponse
            html.append("<div class='modal fade' id='replyModal").append(notification.getId()).append("' tabindex='-1' aria-labelledby='replyModalLabel").append(notification.getId()).append("' aria-hidden='true'>")
                    .append("<div class='modal-dialog'><div class='modal-content'><div class='modal-header'>")
                    .append("<h5 class='modal-title' id='replyModalLabel").append(notification.getId()).append("'>Répondre à la notification</h5>")
                    .append("<button type='button' class='btn-close' data-bs-dismiss='modal' aria-label='Close'></button>")
                    .append("</div>")
                    .append("<div class='modal-body'>")
                    .append("<p>").append(notification.getMessage()).append("</p>")
                    .append("<textarea id='replyMessage").append(notification.getId()).append("' class='form-control' rows='4' placeholder='Tapez votre réponse...'></textarea>")
                    .append("</div>")
                    .append("<div class='modal-footer'>")
                    .append("<button type='button' class='btn btn-secondary' data-bs-dismiss='modal'>Fermer</button>")
                    .append("<button type='button' class='btn btn-primary' onclick='sendReply(").append(notification.getId()).append(")'>Envoyer</button>")
                    .append("</div></div></div></div>");
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
                notification.setSentAt(LocalDateTime.now());

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
        dto.setLu(notification.getIsRead());
        dto.setReservationId(reservationId); // Ajoutez l'ID de la réservation au DTO
        return dto;
    }



    // Méthode pour obtenir les utilisateurs avec des notifications
    public List<User> getAllUsersWithNotifications() {
        List<Notification> notifications = notificationRepository.findAll();
        return notifications.stream()
                .map(Notification::getFromUser)
                .distinct()
                .collect(Collectors.toList());
    }

    public int getTotalNotifications() {
        return (int) notificationRepository.count();
    }


    public Map<String, Long> getNotificationsByDay() {
        return notificationRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        notification -> notification.getSentAt().toLocalDate().toString(),
                        Collectors.counting()
                ));
    }

    public void sendNotification(User toUser, Car car, String message) {
        // Rechercher un administrateur par rôle
        Optional<User> adminUserOptional = userService.findByRole("ADMIN").stream().findFirst();

        if (adminUserOptional.isEmpty()) {
            throw new IllegalStateException("Utilisateur administrateur non trouvé");
        }

        User adminUser = adminUserOptional.get();

        // Création de la notification
        Notification notification = new Notification();
        notification.setToUser(toUser); // L'utilisateur qui reçoit la notification
        notification.setFromUser(adminUser); // L'utilisateur qui envoie la notification est l'admin
        notification.setCar(car); // Ajouter la voiture liée à la réclamation
        notification.setMessage(message); // Le message de la notification
        notification.setIsRead(false); // La notification n'est pas encore lue
        notification.setSentAt(LocalDateTime.now()); // Date de création de la notification
        notification.setType("RECLAMATION");

        // Sauvegarder la notification dans la base de données
        notificationRepository.save(notification);
    }

}
