package be.iccbxl.tfe.Driveshare.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.Date;

@Data
@Entity
@Table(name = "indisponibilites")
public class Indisponible {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "voiture_id")  // Clé étrangère vers Car
    private Car car;

    @Column(name = "date_debut", nullable = false)
    @Temporal(TemporalType.DATE)
    private LocalDate dateDebut;

    @Column(name = "date_fin", nullable = false)
    @Temporal(TemporalType.DATE)
    private LocalDate dateFin;

}

