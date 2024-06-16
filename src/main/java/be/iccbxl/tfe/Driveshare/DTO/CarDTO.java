package be.iccbxl.tfe.Driveshare.DTO;

import be.iccbxl.tfe.Driveshare.model.*;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Data
public class CarDTO {

    private Long id;

    private String brand;

    private String model;

    private String fuelType;

    private String adresse;

    private int codePostal;

    private String locality;

    private String modeReservation;
    private String plaqueImmatriculation;
    private LocalDate firstImmatriculation;
    private String carteGrisePath;

    private List<CarRental> carRentals;
    private User user;
    private String offre  = "standard";


    // Ajout des champs de prix directement
    private Double lowPrice;
    private Double middlePrice;
    private Double highPrice;
    private Double promo1;
    private Double promo2;
    private Boolean isPromotion;

    // Nouveaux champs pour le jour, mois et année de la première immatriculation
    private Integer day;
    private Integer month;
    private Integer year;

    // Nouveaux attributs pour les identifiants des features
    private Long boiteId;
    private Long compteurId;
    private Long placesId;
    private Long portesId;

    private List<Long> equipmentIds;
    private List<String> conditions;

    // Ajout de l'identifiant de la catégorie
    private Long categoryId;

    //indisponibilités
    private List<String> indisponibleDatesStart;
    private List<String> indisponibleDatesEnd;

    // Pour les fichiers
    private MultipartFile registrationCard;
    private MultipartFile identityRecto;
    private MultipartFile identityVerso;

    private List<MultipartFile> photos;


    public LocalDate getFirstImmatriculation() {
        if (day != null && month != null && year != null) {
            return LocalDate.of(year, month, day);
        }
        return null;
    }

}
