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

    // Ajouter ces champs pour l'utilisateur
    private String userFirstName;
    private String userLastName;
    // Ajoutez ce champ
    private LocalDate dateFinLocation;



    // Ajouter les informations sur les gains pour l'utilisateur
    private GainDTO gainDTO;  // Peut être null si aucun gain

    // Méthode pour vérifier si le paiement a été effectué
    public boolean isPaid() {
        LocalDate paymentDueDate = dateFinLocation.plus(3, ChronoUnit.DAYS);
        return LocalDate.now().isAfter(paymentDueDate) && "Paid".equalsIgnoreCase(statut);
    }
}
