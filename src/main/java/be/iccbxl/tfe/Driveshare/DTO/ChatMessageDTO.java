package be.iccbxl.tfe.Driveshare.DTO;

import lombok.Data;

@Data
public class ChatMessageDTO {
    private String content;
    private Long fromUserId;
    private Long toUserId;
    private Long reservationId;
    private String fromUserNom; // Ajoutez ce champ

}
