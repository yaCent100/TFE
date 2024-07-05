package be.iccbxl.tfe.Driveshare.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Data
@Entity
@Table(name="features")
public class Feature {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="Nom")
    private String name;

    @Column(name="Description")
    private String description;

    @ManyToMany(mappedBy = "features")
    private List<Car> cars;

}

