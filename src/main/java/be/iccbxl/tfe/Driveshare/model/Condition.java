package be.iccbxl.tfe.Driveshare.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name="conditions")
public class Condition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="Description")
    private String condition;

    @ManyToOne
    @JoinColumn(name = "VoitureID")
    private Car car;
}
