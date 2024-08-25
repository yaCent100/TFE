package be.iccbxl.tfe.Driveshare.DTO;

import lombok.Data;
import java.util.Map;

@Data
public class EvaluationDashboardDTO {
    private int totalEvaluations;
    private double averageRating;
    private int totalUsers;
    private Map<String, Long> evaluationsByDay;

    public EvaluationDashboardDTO(int totalEvaluations, double averageRating, int totalUsers, Map<String, Long> evaluationsByDay) {
        this.totalEvaluations = totalEvaluations;
        this.averageRating = averageRating;
        this.totalUsers = totalUsers;
        this.evaluationsByDay = evaluationsByDay;
    }
}
