package be.iccbxl.tfe.Driveshare.DTO;

import be.iccbxl.tfe.Driveshare.model.Car;
import jakarta.persistence.*;
import lombok.Data;

@Data
public class PriceDTO {


    private Long id;

    private Double highPrice;

    private Double middlePrice;

    private Double lowPrice;

    private Double promo1;  // Pourcentage de réduction si en promotion

    private Double promo2;

}
