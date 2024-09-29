package be.iccbxl.tfe.Driveshare.DTO;

import be.iccbxl.tfe.Driveshare.model.Car;
import jakarta.persistence.*;
import lombok.Data;

@Data
public class PriceDTO {


    private Long id;

    private double highPrice;

    private double middlePrice;

    private double lowPrice;

    private double promo1;  // Pourcentage de réduction si en promotion

    private double promo2;

    private Long carId;

    public PriceDTO(double middlePrice) {
    }

    public PriceDTO() {
    }
}
