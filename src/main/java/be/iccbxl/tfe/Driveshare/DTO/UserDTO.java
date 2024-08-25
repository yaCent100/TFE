package be.iccbxl.tfe.Driveshare.DTO;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class UserDTO {

    private Long id;
    private String nom;
    private String prenom;
    private String email;
    private String adresse;
    private String locality;
    private String codePostal;
    private String phone;
    private String password;
    private String photoUrl;
    private String iban;
    private String bic;
    private LocalDateTime createdAt;
    private List<CarDTO> ownedCars;
    private List<RoleDTO> roles;
    private List<ReservationDTO> reservations;
    private List<DocumentDTO> documents;
    private List<NotificationDTO> notificationsSent;
    private List<NotificationDTO> notificationsReceived;

    public UserDTO(Long id, String firstName, String lastName, String photoUrl) {
        this.id=id;
        this.prenom=firstName;
        this.nom=lastName;
        this.photoUrl=photoUrl;
    }

    public UserDTO() {

    }
}
