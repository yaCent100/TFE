package be.iccbxl.tfe.Driveshare.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
@Entity
@Data
@Table(name = "Prices")
public class Price {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(mappedBy = "price")
    private Car car;

    @Column(name = "high_price")
    private double highPrice;

    @Column(name = "middle_price")
    private double middlePrice;

    @Column(name = "low_price")
    private double lowPrice;

    @Column(name = "promo_1")
    private double promo1;  // Pourcentage de réduction si en promotion

    @Column(name = "promo_2")
    private double promo2;


}
