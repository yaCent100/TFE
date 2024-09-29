package be.iccbxl.tfe.Driveshare.DTO;

import lombok.Data;
import java.util.Map;

@Data
public class EvaluationDashboardDTO {
    private int totalEvaluations;
    private double averageRating;
    private int totalUsers;
    private int totalReservations; // Nombre total de réservations
    private double evaluationReservationPercentage; // Pourcentage des évaluations par rapport aux réservations
    private double evaluationFiveStarsPercentage; // Pourcentage des évaluations à 5 étoiles
    private Map<String, Long> evaluationsByDay;

    // Ajout de evaluationFiveStarsPercentage dans le constructeur
    public EvaluationDashboardDTO(int totalEvaluations, double averageRating, int totalUsers, int totalReservations, double evaluationReservationPercentage, double evaluationFiveStarsPercentage, Map<String, Long> evaluationsByDay) {
        this.totalEvaluations = totalEvaluations;
        this.averageRating = averageRating;
        this.totalUsers = totalUsers;
        this.totalReservations = totalReservations;
        this.evaluationReservationPercentage = evaluationReservationPercentage;
        this.evaluationFiveStarsPercentage = evaluationFiveStarsPercentage; // Nouvelle propriété pour 5 étoiles
        this.evaluationsByDay = evaluationsByDay;
    }
}
