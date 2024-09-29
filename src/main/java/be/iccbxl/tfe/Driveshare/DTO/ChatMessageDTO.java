package be.iccbxl.tfe.Driveshare.DTO;

import be.iccbxl.tfe.Driveshare.model.Reservation;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChatMessageDTO {
    private String content;
    private Long fromUserId;
    private Long toUserId;
    private Long reservationId; // Utilisez un ID pour la réservation
    private String fromUserNom; // Nom de l'utilisateur qui a envoyé le message
    private String toUserNom; // Nom de l'utilisateur qui a envoyé le message
    private String carBrand; // Marque de la voiture
    private String carModel; // Modèle de la voiture
    private String carImage; // Première photo de la voiture
    private LocalDateTime sentAt;
    private String profileImageUrl;
    private String profileImageUrl2;


}
