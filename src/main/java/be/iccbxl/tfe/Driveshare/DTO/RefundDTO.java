package be.iccbxl.tfe.Driveshare.DTO;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RefundDTO {
    private Long id;
    private double amount;
    private LocalDateTime refundDate;
    private double refundPercentage;
    private Long paymentId;
}
