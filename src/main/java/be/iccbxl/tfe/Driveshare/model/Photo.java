package be.iccbxl.tfe.Driveshare.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name="photos")
public class Photo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="url")
    private String url;

    @ManyToOne
    @JoinColumn(name = "VoitureID")
    private Car car;

}
