package be.iccbxl.tfe.Driveshare.DTO;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DashboardKpiDTO {
    private BigDecimal totalRevenue;
    private BigDecimal totalBenefit;
    private long totalUsers;
    private long totalReservations;

    // Getters and Setters

}
