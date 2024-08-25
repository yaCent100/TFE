package be.iccbxl.tfe.Driveshare.DTO;

import be.iccbxl.tfe.Driveshare.model.Car;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CarReservationKpiDTO {
    private Long carId;
    private String carModel;
    private String carBrand;
    private String photoUrl;
    private Long reservationCount;
    private BigDecimal totalRevenue;
    private int trend;

    public CarReservationKpiDTO(Long carId, String carModel,String carBrand, String carPhoto, Long reservationCount, BigDecimal totalRevenue, int trend) {
        this.carId = carId;
        this.carModel = carModel;
        this.carBrand = carBrand;
        this.photoUrl = carPhoto;
        this.reservationCount = reservationCount;
        this.totalRevenue = totalRevenue;
        this.trend = trend;
    }


    public static CarReservationKpiDTO toCarReservationKpiDTO(Car car, long reservationCount, BigDecimal totalRevenue, int trend) {
        // Récupérer la première photo, s'il y en a
        String carPhotoUrl = car.getPhotos() != null && !car.getPhotos().isEmpty() ? car.getPhotos().get(0).getUrl() : "default.png";

        return new CarReservationKpiDTO(
                car.getId(),
                car.getBrand(),
                car.getModel(),
                carPhotoUrl,
                reservationCount,
                totalRevenue,
                trend
        );
    }
}
