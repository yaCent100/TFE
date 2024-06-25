package be.iccbxl.tfe.Driveshare.DTO;

import lombok.Data;

import java.time.LocalDate;

@Data
public class IndisponibleDTO {
    private Long id;
    private LocalDate startDate;
    private LocalDate endDate;
    private Long voitureId;

    // getters and setters
}
