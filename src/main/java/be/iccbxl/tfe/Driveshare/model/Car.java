package be.iccbxl.tfe.Driveshare.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Data
@Entity
@Table(name="voitures")
public class Car {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="Marque")
    private String brand;

    @Column(name="Modele")
    private String model;

    @Column(name="Annee")
    private int year;

    @Column(name="Carburant")
    private String fuelType;

    @ManyToOne
    @JoinColumn(name = "UserID")
    private User owner;

    @ManyToOne
    @JoinColumn(name = "CategorieID")
    private Category category;

    @OneToMany(cascade = CascadeType.ALL)
    private List<Condition> conditions;

    @OneToMany(cascade = CascadeType.ALL)
    private List<Photo> photos;

    @ManyToMany
    @JoinTable(
            name = "voitures_caracteristiques",
            joinColumns = @JoinColumn(name = "voiture_id"),
            inverseJoinColumns = @JoinColumn(name = "caracteristique_id")
    )
    private List<Feature> features;

    @ManyToMany
    @JoinTable(
            name = "voitures_equipements",
            joinColumns = @JoinColumn(name = "voiture_id"),
            inverseJoinColumns = @JoinColumn(name = "equipement_id")
    )
    private List<Equipment> equipments;


    public void addCondition(Condition condition) {
        conditions.add(condition);
    }

    public void removeCondition(Condition condition) {
        conditions.remove(condition);
    }

    public void addPhoto(Photo photo) {
        photos.add(photo);
    }

    public void removePhoto(Photo photo) {
        photos.remove(photo);
    }

    public void addFeature(Feature feature) {
        features.add(feature);
    }

    public void removeFeature(Feature feature) {
        features.remove(feature);
    }

    public void addEquipment(Equipment equipment) {
        equipments.add(equipment);
    }

    public void removeEquipment(Equipment equipment) {
        equipments.remove(equipment);
    }



}
