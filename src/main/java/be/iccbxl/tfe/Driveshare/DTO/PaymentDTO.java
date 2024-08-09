package be.iccbxl.tfe.Driveshare.DTO;


import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PaymentDTO {

    private Long id;
    private double prixTotal;
    private String statut;
    private String paiementMode;
    private double prixPourDriveShare;
    private double prixPourUser;
    private LocalDateTime createdAt;
    private Long reservationId;
    private RefundDTO refundDTO;
}
