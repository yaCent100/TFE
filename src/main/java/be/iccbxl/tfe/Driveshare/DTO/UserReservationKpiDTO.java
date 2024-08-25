package be.iccbxl.tfe.Driveshare.DTO;

import lombok.Data;

@Data
public class UserReservationKpiDTO {
    private Long userId;
    private String firstName;
    private String lastName;
    private Long reservationCount;

    public UserReservationKpiDTO(Long userId, String firstName, String lastName, Long reservationCount) {
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.reservationCount = reservationCount;
    }
}
