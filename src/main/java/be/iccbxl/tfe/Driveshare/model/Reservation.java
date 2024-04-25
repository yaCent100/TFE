package be.iccbxl.tfe.Driveshare.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "debut_location")
    private LocalDate debutLocation;

    @Column(name = "fin_location")
    private LocalDate finLocation;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @ManyToOne
    private User user;

    @ManyToOne
    private Car car;

    private String statut;

    private int nbJours;
}
