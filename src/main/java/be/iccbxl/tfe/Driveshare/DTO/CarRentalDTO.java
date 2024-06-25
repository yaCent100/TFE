package be.iccbxl.tfe.Driveshare.DTO;

import lombok.Data;

import java.util.List;

@Data
public class CarRentalDTO {
    private Long id;
    private List<ReservationDTO> reservations;

    // getters and setters
}