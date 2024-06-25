package be.iccbxl.tfe.Driveshare.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "paiement")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "prix_total", nullable = false)
    private Double prixTotal;

    @Column(name = "Statut", nullable = false)
    private String statut;

    @Column(name = "paiement_mode", nullable = false)
    private String paiementMode;

    @Column(name = "created_at", nullable = false)
    private LocalDate createdAt;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ReservationID")
    private Reservation reservation;

}
