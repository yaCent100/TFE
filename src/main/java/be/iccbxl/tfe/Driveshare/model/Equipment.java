package be.iccbxl.tfe.Driveshare.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Data
@Entity
@Table(name="equipements")
public class Equipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="description")
    private String equipment;

    @Column(name="icon")
    private String icone;

    @ManyToMany(mappedBy = "equipments")
    private List<Car> cars;

}
