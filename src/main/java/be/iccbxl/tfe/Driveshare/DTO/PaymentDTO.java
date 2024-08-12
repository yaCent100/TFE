package be.iccbxl.tfe.Driveshare.DTO;


import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

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


    // Ajoutez ce champ
    private LocalDate dateFinLocation;

    // Méthode pour vérifier si le paiement a été effectué
    public boolean isPaid() {
        LocalDate paymentDueDate = dateFinLocation.plus(3, ChronoUnit.DAYS);
        return LocalDate.now().isAfter(paymentDueDate) && "Paid".equalsIgnoreCase(statut);
    }
}
