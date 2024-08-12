package be.iccbxl.tfe.Driveshare.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "gains")
public class Gain {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;  // Relation OneToOne avec Payment

    @Column(name = "montant_gain", nullable = false)
    private double montantGain;

    @Column(name = "date_gain", nullable = false)
    private LocalDateTime dateGain;

    @Column(name = "statut", nullable = false)
    private String statut;

    @Column(name = "description")
    private String description;



}
