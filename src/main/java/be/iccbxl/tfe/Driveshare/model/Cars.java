package be.iccbxl.tfe.Driveshare.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name="voitures")
public class Cars {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String brand;

    private String model;

    private int year;

    private String fuelType;

    @ManyToOne
    private User owner;

    @ManyToOne
    private Category category;

    @OneToMany(cascade = CascadeType.ALL)
    private List<Condition> conditions;

    @OneToMany(cascade = CascadeType.ALL)
    private List<Photo> photos;

    @OneToMany(cascade = CascadeType.ALL)
    private List<Feature> features;

    @OneToMany(cascade = CascadeType.ALL)
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
