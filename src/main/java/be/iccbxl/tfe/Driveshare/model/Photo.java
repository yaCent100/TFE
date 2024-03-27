package be.iccbxl.tfe.Driveshare.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name="photo")
public class Photo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="filePath")
    private String photo;

    @ManyToOne
    private Cars car;

}
