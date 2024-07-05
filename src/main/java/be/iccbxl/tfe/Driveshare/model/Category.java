package be.iccbxl.tfe.Driveshare.model;

import jakarta.persistence.*;
import lombok.Data;


@Data
@Entity
@Table(name="categorie")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="category")
    private String category;


}
