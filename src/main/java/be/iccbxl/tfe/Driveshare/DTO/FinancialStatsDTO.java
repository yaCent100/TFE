package be.iccbxl.tfe.Driveshare.DTO;

import lombok.Data;

import java.util.Map;

@Data
public class FinancialStatsDTO {
    private Map<String, Double> benefitByDay;
    private Map<String, Double> refundsByDay;
    private Map<String, Double> userGeneratedRevenueByDay;

    public FinancialStatsDTO(Map<String, Double> benefitByDay, Map<String, Double> refundsByDay, Map<String, Double> userGeneratedRevenueByDay) {
        this.benefitByDay = benefitByDay;
        this.refundsByDay = refundsByDay;
        this.userGeneratedRevenueByDay = userGeneratedRevenueByDay;
    }
}

