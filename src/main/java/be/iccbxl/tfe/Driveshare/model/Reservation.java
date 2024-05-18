package be.iccbxl.tfe.Driveshare.model;

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

    @Column(name = "DebutLocation")
    private LocalDate debutLocation;

    @Column(name = "FinLocation")
    private LocalDate finLocation;

    @Column(name = "CreatedAt")
    private LocalDateTime createdAt;

    @Column(name = "statut")
    private String statut;

    @Column(name = "nb_jours")
    private int nbJours;
}
