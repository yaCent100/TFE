package be.iccbxl.tfe.Driveshare.classes;

import jakarta.persistence.Column;
import lombok.Data;

@Data
public class CarDTO {

    private Long id;

    private String brand;

    private String model;

    private int year;

    private String fuelType;

    private String adresse;

    private String codePostal;

    private String locality;

    private double price;

    private double miles;


}
