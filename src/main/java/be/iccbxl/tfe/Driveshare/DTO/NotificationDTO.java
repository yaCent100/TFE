package be.iccbxl.tfe.Driveshare.DTO;

import be.iccbxl.tfe.Driveshare.model.Notification;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NotificationDTO {

    private String message;

    private Long fromUserId;

    private Long toUserId;

    private Long carId;

    private String type;

    private boolean lu;

    private Long reservationId; // L'ID de la réservation, utilisé uniquement pour la logique métier

    private String carImage;

    private String fromUserNom;

    private String fromUserProfil;

    private String toUserNom;

    private String toUserProfil;

    private String carBrand;

    private String carModel;

    private LocalDateTime sendAt;







}


