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

    @ManyToOne
    @JoinColumn(name = "category_id")  // Ceci est la clé étrangère
    private Category category;

    @Column
    private Double price;

    @Column
    private String season;

    @Column(name = "is_promotion")
    private Boolean isPromotion;

    @Column(name = "promotion_description")
    private String promotionDescription;
}
