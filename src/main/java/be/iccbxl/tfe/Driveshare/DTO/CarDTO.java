package be.iccbxl.tfe.Driveshare.DTO;

import be.iccbxl.tfe.Driveshare.model.*;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class CarDTO {

    private Long id;

    private String brand;

    private String model;

    private String fuelType;

    private String adresse;
    private  String url;



    private int codePostal;

    private String locality;

    private String modeReservation;
    private String plaqueImmatriculation;
    private LocalDate firstImmatriculation;
    private String carteGrisePath;

    private List<CarRentalDTO> carRentals;
    private User user;
    private String offre  = "standard";

    private PriceDTO price; // Inclure le prix

    // Nouveaux champs pour le jour, mois et année de la première immatriculation
    private Integer day;
    private Integer month;
    private Integer year;

    // Nouveaux attributs pour les identifiants des features
    private List<FeatureDTO> features;

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

    private List<IndisponibleDTO> unavailables;

    // Pour les fichiers
    private MultipartFile registrationCard;
    private MultipartFile identityRecto;
    private MultipartFile identityVerso;

    private List<MultipartFile> photos; // Modifier en liste de MultipartFile
    private List<String> photoUrl;  // Ajouter ce champ pour l'URL de la photo


    /* DISTANCE */
    private double latitude;
    private double longitude;
    private double distance;




    public CarDTO(Long id, String brand, String model, String s, double distance, String url, String adresse, int codePostal, String locality, List<FeatureDTO> featureDTOs) {
    }


    public LocalDate getFirstImmatriculation() {
        if (day != null && month != null && year != null) {
            return LocalDate.of(year, month, day);
        }
        return null;
    }

    public CarDTO(Long id, String marque, String modele, String modeReservation, Double middlePrice, String url, String adresse, int codePostal, String locality, String fuelType) {
        this.id = id;
        this.brand = marque;
        this.model = modele;
        this.modeReservation = modeReservation;
        this.url = url;
        this.adresse = adresse;
        this.codePostal = codePostal;
        this.locality = locality;
        this.fuelType = fuelType;
    }

    public CarDTO() {}


    public CarDTO(Car car) {
        this.id = car.getId();
        this.brand = car.getBrand();
        this.model = car.getModel();
        this.fuelType = car.getFuelType();
        this.adresse = car.getAdresse();
        this.codePostal = car.getCodePostal();
        this.locality = car.getLocality();
        this.plaqueImmatriculation = car.getPlaqueImmatriculation();
        this.firstImmatriculation = car.getFirstImmatriculation();
        this.categoryId = car.getCategory().getId();

        if (car.getFirstImmatriculation() != null) {
            this.day = car.getFirstImmatriculation().getDayOfMonth();
            this.month = car.getFirstImmatriculation().getMonthValue();
            this.year = car.getFirstImmatriculation().getYear();
        }

        this.boiteId = car.getFeatures().stream().filter(f -> f.getName().equals("boite")).findFirst().map(Feature::getId).orElse(null);
        this.compteurId = car.getFeatures().stream().filter(f -> f.getName().equals("compteur")).findFirst().map(Feature::getId).orElse(null);
        this.placesId = car.getFeatures().stream().filter(f -> f.getName().equals("places")).findFirst().map(Feature::getId).orElse(null);
        this.portesId = car.getFeatures().stream().filter(f -> f.getName().equals("portes")).findFirst().map(Feature::getId).orElse(null);

        this.equipmentIds = car.getEquipments().stream().map(Equipment::getId).collect(Collectors.toList());

    }

    // Constructor
    public CarDTO(Car car, double distance) {
        this.id = car.getId();
        this.brand = car.getBrand();
        this.model = car.getModel();
        this.adresse = car.getAdresse();
        this.codePostal = car.getCodePostal();
        this.locality = car.getLocality();
        this.latitude = car.getLatitude();
        this.longitude = car.getLongitude();
        this.distance = distance;
        this.plaqueImmatriculation = car.getPlaqueImmatriculation();
        this.photoUrl = car.getPhotos() != null && !car.getPhotos().isEmpty()
                ? car.getPhotos().stream().map(Photo::getUrl).collect(Collectors.toList())
                : null;
    }



}
