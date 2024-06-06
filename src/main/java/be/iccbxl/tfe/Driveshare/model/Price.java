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

    @OneToOne(mappedBy = "price", cascade = CascadeType.ALL)
    private Car car;

    @Column(name = "high_price")
    private Double highPrice;

    @Column(name = "middle_price")
    private Double middlePrice;

    @Column(name = "low_price")
    private Double lowPrice;

    @Column(name = "is_promotion")
    private Boolean isPromotion;

    @Column(name = "promo_1")
    private Double promo1;  // Pourcentage de réduction si en promotion

    @Column(name = "promo_2")
    private Double promo2;


}
