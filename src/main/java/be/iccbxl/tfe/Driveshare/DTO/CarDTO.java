package be.iccbxl.tfe.Driveshare.DTO;

import be.iccbxl.tfe.Driveshare.model.*;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.Collections;
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

    private String codePostal;

    private String locality;

    private String modeReservation;
    private String plaqueImmatriculation;
    private LocalDate firstImmatriculation;
    private String carteGrisePath;

    private List<ReservationDTO> reservationDTOS;
    private UserDTO user;
    private String offre  = "standard";

    private double displayPrice;

    private PriceDTO price = new PriceDTO(); //// Inclure le prix

    private double averageRating; // Note moyenne
    private int reviewCount; // Nombre d'avis


    // Nouveaux champs pour le jour, mois et année de la première immatriculation
    private Integer day;
    private Integer month;
    private Integer year;

    // Nouveaux attributs pour les identifiants des features
    private List<FeatureDTO> features;
    private List<EquipmentDTO> equipments;

    private Long boiteId;
    private String boite;
    private String compteur;
    private String places;
    private String portes;

    private Long compteurId;
    private Long placesId;
    private Long portesId;

    private List<Long> equipmentIds;
    private List<String> conditions;
    private List<ConditionDTO> conditionsDTOs;


    // Ajout de l'identifiant de la catégorie
    private Long categoryId;
    private String categoryName;

    //indisponibilités
    private List<String> indisponibleDatesStart;
    private List<String> indisponibleDatesEnd;

    private List<IndisponibleDTO> unavailables;

    // Pour les fichiers
    private MultipartFile registrationCard;
    private MultipartFile identityRecto;
    private MultipartFile identityVerso;

    private String registrationCardUrl;


    private List<MultipartFile> photos; // Modifier en liste de MultipartFile
    private List<String> photoUrl;  // Ajouter ce champ pour l'URL de la photo


    /* DISTANCE */
    private double latitude;
    private double longitude;
    private double distance;


    private Boolean online = false;



    public CarDTO(Long id, String brand, String model, String s, double distance, String url, String adresse, String codePostal, String locality, List<FeatureDTO> featureDTOs) {

    }



    public LocalDate getFirstImmatriculation() {
        if (day != null && month != null && year != null) {
            return LocalDate.of(year, month, day);
        }
        return null;
    }


    //CONSTRUCTEUR HOME TOP-RATED
    public CarDTO(Long id, String brand, String model, double displayPrice, double averageRating) {
        this.id = id;
        this.brand = brand;
        this.model = model;
        this.displayPrice = displayPrice;
        this.averageRating = averageRating;
    }

    public CarDTO(Long id, String marque, String modele, String modeReservation, String url, String adresse, String codePostal, String locality, String fuelType) {
        this.id = id;
        this.brand = marque;
        this.model = modele;
        this.modeReservation = modeReservation;
        this.url = url;
        this.adresse = adresse;
        this.codePostal = codePostal;
        this.locality = locality;
        this.fuelType = fuelType;
        this.price = new PriceDTO(); // Initialisation par défaut

    }

    public CarDTO() {}


    public CarDTO(Car car) {
        this.id = car.getId();
        this.brand = car.getBrand();
        this.model = car.getModel();
        this.fuelType = car.getFuelType();
        this.adresse = car.getAdresse();
        this.codePostal = car.getPostalCode();
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
        this.codePostal = car.getPostalCode();
        this.locality = car.getLocality();
        this.latitude = car.getLatitude();
        this.longitude = car.getLongitude();
        this.distance = distance;
        this.plaqueImmatriculation = car.getPlaqueImmatriculation();

        this.categoryName=car.getCategory().getCategory();

        // Gestion des URLs des photos
        this.photoUrl = (car.getPhotos() != null && !car.getPhotos().isEmpty())
                ? car.getPhotos().stream().map(Photo::getUrl).collect(Collectors.toList())
                : Collections.emptyList();

        // Mapping des features à FeatureDTO
        if (car.getFeatures() != null) {
            this.features = car.getFeatures().stream()
                    .map(MapperDTO::toFeatureDTO)
                    .collect(Collectors.toList());
        } else {
            this.features = Collections.emptyList();  // Liste vide si aucune feature
        }

        this.fuelType = car.getFuelType();

        this.displayPrice = car.getDisplayPrice();
        this.modeReservation=car.getModeReservation();
        // Ajouter les réservations à la voiture
        if (car.getReservations() != null) {
            this.reservationDTOS = car.getReservations().stream()
                    .map(MapperDTO::toReservationDTO)
                    .collect(Collectors.toList());
        } else {
            this.reservationDTOS = Collections.emptyList();  // Liste vide si aucune réservation
        }
    }

    // Vous pouvez également définir un setter pour la liste des features si vous avez besoin
    public void setFeatures(List<FeatureDTO> features) {
        this.features = features;
    }




    public CarDTO(Car car, double averageRating, int reviewCount, double distance, double displayPrice) {
        this(car, distance);  // Appel au constructeur de base pour réutiliser le code commun
        this.averageRating = averageRating;
        this.reviewCount = reviewCount;
        this.displayPrice = displayPrice;
    }




}
