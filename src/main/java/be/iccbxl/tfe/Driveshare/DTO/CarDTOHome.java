package be.iccbxl.tfe.Driveshare.DTO;

import lombok.Data;

@Data
public class CarDTOHome {

    private Long id;
    private String brand;
    private String model;
    private String postalCode;
    private String adresse;
    private String locality;
    private String photoUrl;
    private double displayPrice;
    private double averageRating;

    public CarDTOHome(Long id, String brand, String model, String locality, String photoUrl , double displayPrice, double averageRating) {
        this.id = id;
        this.brand = brand;
        this.model = model;
        this.locality= locality;
        this.photoUrl = photoUrl;
        this.displayPrice = displayPrice;
        this.averageRating = averageRating;
    }

    public CarDTOHome(Long id, String brand, String model,String adresse, String postalCode,String locality) {
        this.id = id;
        this.brand = brand;
        this.model = model;
        this.adresse=adresse;
        this.postalCode=postalCode;
        this.locality= locality;
    }
}
