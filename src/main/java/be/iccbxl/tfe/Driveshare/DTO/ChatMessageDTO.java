package be.iccbxl.tfe.Driveshare.DTO;

import be.iccbxl.tfe.Driveshare.model.Reservation;
import lombok.Data;

@Data
public class ChatMessageDTO {
    private String content;
    private Long fromUserId;
    private Long toUserId;
    private Long reservationId; // Utilisez un ID pour la réservation
    private String fromUserNom; // Ajoutez ce champ

}
