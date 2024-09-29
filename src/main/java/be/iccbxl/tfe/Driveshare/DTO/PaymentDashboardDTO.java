package be.iccbxl.tfe.Driveshare.DTO;

import lombok.Data;

@Data
public class PaymentDashboardDTO {
    private int totalTransactions; // Nombre total de transactions
    private double cancellationPercentage; // Pourcentage d'annulations
    private double totalRefunds; // Montant total des remboursements
    private double userGeneratedRevenue; // Montant généré par les utilisateurs

    public PaymentDashboardDTO(int totalTransactions, double cancellationPercentage, double totalRefunds, double userGeneratedRevenue) {
        this.totalTransactions = totalTransactions;
        this.cancellationPercentage = cancellationPercentage;
        this.totalRefunds = totalRefunds;
        this.userGeneratedRevenue = userGeneratedRevenue;
    }
}
