package be.iccbxl.tfe.Driveshare.DTO;

import lombok.Data;

@Data
public class NotificationDTO {

    private String message;

    private Long fromUserId;

    private Long toUserId;

    private Long carId;

    private String type;

    private boolean lu;

}
