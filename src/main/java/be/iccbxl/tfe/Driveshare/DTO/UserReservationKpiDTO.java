package be.iccbxl.tfe.Driveshare.DTO;

import lombok.Data;

@Data
public class UserReservationKpiDTO {
    private Long userId;
    private String firstName;
    private String lastName;
    private Long reservationCount;
    private String photoUrl;
    private String trend; // Nouveau champ pour la tendance

    public UserReservationKpiDTO(Long userId, String firstName, String lastName, Long reservationCount, String photoUrl, String trend) {
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.reservationCount = reservationCount;
        this.photoUrl = photoUrl;
        this.trend = trend;
    }

    public UserReservationKpiDTO() {

    }

}
