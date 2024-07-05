package be.iccbxl.tfe.Driveshare.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name="reservations")
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rental_id", nullable = false)
    private CarRental carRental;

    @Column(name = "debut_location")
    private LocalDate debutLocation;

    @Column(name = "fin_location")
    private LocalDate finLocation;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "statut")
    private String statut;

    @Column(name = "nb_jours")
    private int nbJours;

    @OneToOne(mappedBy = "reservation", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Payment payment;
}
