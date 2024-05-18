package be.iccbxl.tfe.Driveshare.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
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

    @Column(name="Adresse")
    private String adresse;

    @Column(name="code_postal")
    private int codePostal;

    @Column(name="locality")
    private String locality;

    @Column(name="prix")
    private double price;

    @Column(name="kilometrage")
    private double miles;

    @ManyToOne
    @JoinColumn(name = "UserID")
    private User user;

    @ManyToOne
    @JoinColumn(name = "CategorieID")
    private Category category;

    @OneToMany(mappedBy = "car", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Condition> conditions;

    @OneToMany(mappedBy = "car", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Photo> photos;

    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinTable(
            name = "voitures_caracteristiques",
            joinColumns = @JoinColumn(name = "VoitureID"),
            inverseJoinColumns = @JoinColumn(name = "CaracteristiqueID")
    )
    private List<Feature> features;

    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinTable(
            name = "voitures_equipements",
            joinColumns = @JoinColumn(name = "VoitureID"),
            inverseJoinColumns = @JoinColumn(name = "EquipementID")
    )
    private List<Equipment> equipments;

    @OneToMany(mappedBy = "car")
    private List<CarRental> carRentals;


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


    @Override
    public String toString() {
        return "Car{" +
                "id=" + id +
                ", brand='" + brand + '\'' +
                ", model='" + model + '\'' +
                ", year=" + year +
                '}';
    }
}


