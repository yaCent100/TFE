package be.iccbxl.tfe.Driveshare.service.serviceImpl;

import be.iccbxl.tfe.Driveshare.DTO.CarDTO;
import be.iccbxl.tfe.Driveshare.DTO.CarMapper;
import be.iccbxl.tfe.Driveshare.model.*;
import be.iccbxl.tfe.Driveshare.repository.*;
import be.iccbxl.tfe.Driveshare.service.CarServiceI;
import com.google.maps.model.GeocodingResult;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CarService implements CarServiceI {

    @Autowired
    private CarRepository carRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private DocumentService documentService;

    @Autowired
    private FeatureRepository featureRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private PriceRepository priceRepository;

    @Autowired
    private EquipmentService equipmentService;

    @Autowired
    private GeocodingService geocodingService;

    private final Logger logger = LoggerFactory.getLogger(CarService.class);


    @Override
    public List<Car> getAllCars() {
        List<Car> cars = new ArrayList<>();
        carRepository.findAll().forEach(cars::add);
        return cars;
    }

    @Override
    public Car getCarById(Long id) {
        Optional<Car> optionalCar = carRepository.findById(id);
        return optionalCar.orElse(null);
    }

    @Override
    public Car addUser(Car car) {
        return null;
    }

    @Override
    public Car saveCar(Car car) {
        return carRepository.save(car);
    }

    @Override
    public Car updateCar(Long id, Car car) {
        Optional<Car> optionalCar = carRepository.findById(id);
        if (optionalCar.isPresent()) {
            Car existingCar = optionalCar.get();
            existingCar.setBrand(car.getBrand());
            existingCar.setModel(car.getModel());
            /*....*/
            return carRepository.save(existingCar);
        } else {
            return null;
        }
    }

    @Override
    public void deleteCar(Long id) {
        Car car = carRepository.findById(id).orElse(null);
        if (car != null) {
            carRepository.delete(car);
        }
    }

    @Override
    public double calculateAverageRating(Car car) {
        List<CarRental> carRentals = car.getCarRentals();
        if (carRentals.isEmpty()) {
            return 0.0; // Retourne 0 si aucune location n'est associée à la voiture
        }

        double sum = 0.0;
        int totalEvaluations = 0; // Compte le nombre total d'évaluations pour faire la moyenne

        // Parcourir chaque location de la voiture
        for (CarRental carRental : carRentals) {
            List<Evaluation> evaluations = carRental.getEvaluations(); // Obtenir les évaluations de chaque location
            for (Evaluation evaluation : evaluations) {
                sum += evaluation.getNote(); // Ajouter la note de l'évaluation au total
                totalEvaluations++; // Incrémenter le compteur d'évaluations
            }
        }

        if (totalEvaluations == 0) {
            return 0.0; // Évite la division par zéro si aucune évaluation n'est présente
        }

        // Calculer la moyenne et arrondir à la demi-note la plus proche
        return Math.round((sum / totalEvaluations) * 2) / 2.0;
    }

    @Override
    public Map<Long, Double> getAverageRatingsForCars() {
        Iterable<Car> cars = carRepository.findAll();
        Map<Long, Double> averageRatings = new HashMap<>();

        for (Car car : cars) {
            double averageRating = calculateAverageRating(car);
            averageRatings.put(car.getId(), averageRating);
        }

        return averageRatings;
    }

    @Override
    public Map<Long, Integer> getReviewCountsForCars() {
        Iterable<Car> cars = carRepository.findAll();
        Map<Long, Integer> reviewCounts = new HashMap<>();

        for (Car car : cars) {
            List<CarRental> carRentals = car.getCarRentals(); // Obtenir toutes les locations de la voiture
            int count = 0;

            for (CarRental carRental : carRentals) {
                count += carRental.getEvaluations().size(); // Ajouter le nombre d'évaluations pour chaque location
            }

            reviewCounts.put(car.getId(), count); // Stocker le nombre total d'évaluations pour cette voiture
        }

        return reviewCounts;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CarDTO> searchAvailableCars(String address, double lat, double lng, LocalDate dateDebut, LocalDate dateFin) throws Exception {
        GeocodingResult userLocation = geocodingService.geocode(address);
        if (userLocation == null) {
            throw new Exception("Unable to geocode the provided address.");
        }

        double userLat = userLocation.geometry.location.lat;
        double userLng = userLocation.geometry.location.lng;
        logger.info("User Location: Lat: {}, Lng: {}", userLat, userLng);

        List<Car> allCars = carRepository.findAll();
        logger.info("Total cars retrieved from DB: {}", allCars.size());

        List<Car> availableCars = allCars.stream()
                .filter(car -> isCarAvailableForDates(car, dateDebut, dateFin))
                .filter(car -> isCarNearby(car, userLat, userLng, 10)) // 10 km threshold
                .collect(Collectors.toList());

        logger.info("Nearby available cars: {}", availableCars.size());
        return availableCars.stream().map(CarMapper::toCarDTO).collect(Collectors.toList());
    }

    private boolean isCarAvailableForDates(Car car, LocalDate dateDebut, LocalDate dateFin) {
        List<CarRental> carRentals = car.getCarRentals();

        for (CarRental carRental : carRentals) {
            List<Reservation> reservations = carRental.getReservations();

            for (Reservation reservation : reservations) {
                LocalDate reservationStartDate = reservation.getDebutLocation();
                LocalDate reservationEndDate = reservation.getFinLocation();

                if ((dateDebut.isEqual(reservationStartDate) || dateDebut.isAfter(reservationStartDate)) &&
                        (dateDebut.isEqual(reservationEndDate) || dateDebut.isBefore(reservationEndDate)) ||
                        (dateFin.isEqual(reservationStartDate) || dateFin.isAfter(reservationStartDate)) &&
                                (dateFin.isEqual(reservationEndDate) || dateFin.isBefore(reservationEndDate))) {
                    return false;
                }
            }
        }

        List<Indisponible> indisponibilites = car.getUnavailable();

        for (Indisponible indisponibilite : indisponibilites) {
            LocalDate indisponibiliteStartDate = indisponibilite.getStartDate();
            LocalDate indisponibiliteEndDate = indisponibilite.getEndDate();

            if ((dateDebut.isEqual(indisponibiliteStartDate) || dateDebut.isAfter(indisponibiliteStartDate)) &&
                    (dateDebut.isEqual(indisponibiliteEndDate) || dateDebut.isBefore(indisponibiliteEndDate)) ||
                    (dateFin.isEqual(indisponibiliteStartDate) || dateFin.isAfter(indisponibiliteStartDate)) &&
                            (dateFin.isEqual(indisponibiliteEndDate) || dateFin.isBefore(indisponibiliteEndDate))) {
                return false;
            }
        }

        return true;
    }


    private boolean isCarNearby(Car car, double userLat, double userLng, double distanceThreshold) {
        double carLat = car.getLatitude();
        double carLng = car.getLongitude();
        if (carLat == 0.0 && carLng == 0.0) {
            logger.warn("Car ID: {} has default coordinates (0.0, 0.0), skipping distance check.", car.getId());
            return false;
        }
        logger.info("Car ID: {}, Car Lat: {}, Car Lng: {}", car.getId(), carLat, carLng);
        double distance = calculateHaversineDistance(userLat, userLng, carLat, carLng);
        logger.info("Car ID: {}, Distance: {}", car.getId(), distance);
        return distance <= distanceThreshold;
    }

    private double calculateHaversineDistance(double lat1, double lng1, double lat2, double lng2) {
        final int R = 6371; // Radius of the Earth in kilometers
        double latDistance = Math.toRadians(lat2 - lat1);
        double lngDistance = Math.toRadians(lng2 - lng1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lngDistance / 2) * Math.sin(lngDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }




    @Transactional
    @Override
    public Car createCar(CarDTO carDTO) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentUsername = authentication.getName();
            User user = userRepository.findByEmail(currentUsername);

            if (user == null) {
                throw new RuntimeException("User not found: " + currentUsername);
            }

            Car car = new Car();
            BeanUtils.copyProperties(carDTO, car);
            car.setUser(user);

            if (carDTO.getCategoryId() != null) {
                Category category = categoryRepository.findById(carDTO.getCategoryId()).orElse(null);
                car.setCategory(category);
            }

            List<Feature> features = getFeatures(carDTO);
            car.setFeatures(features);

            List<Equipment> equipments = getEquipments(carDTO);
            car.setEquipments(equipments);

            List<Condition> conditions = getConditions(carDTO, car);
            car.setConditions(conditions);

            Price price = CarMapper.toPrice(carDTO.getPrice()); // Conversion du DTO en entité Price
            car.setPrice(price);

            // Sauvegarder la voiture sans les photos et documents
            car = carRepository.save(car);

            // Gestion des photos après avoir persisté la voiture
            if (carDTO.getPhotos() != null && !carDTO.getPhotos().isEmpty()) {
                List<Photo> photoEntities = new ArrayList<>();
                for (MultipartFile photo : carDTO.getPhotos()) {
                    if (photo != null && !photo.isEmpty()) {
                        String photoUrl = fileStorageService.storeFile(photo, "photo-car");
                        Photo photoEntity = new Photo();
                        photoEntity.setUrl(photoUrl);
                        photoEntity.setCar(car);
                        photoEntities.add(photoEntity);
                    }
                }
                car.setPhotos(photoEntities);
            }

            // Gestion des documents avant de sauvegarder la voiture
            if (carDTO.getRegistrationCard() != null && !carDTO.getRegistrationCard().isEmpty()) {
                String registrationCardPath = fileStorageService.storeFile(carDTO.getRegistrationCard(), "registration");
                car.setCarteGrisePath(registrationCardPath);
            }

            if (carDTO.getIdentityRecto() != null && !carDTO.getIdentityRecto().isEmpty()) {
                String identityRectoPath = fileStorageService.storeFile(carDTO.getIdentityRecto(), "identity");
                Document identityRectoDocument = new Document();
                identityRectoDocument.setUrl(identityRectoPath);
                identityRectoDocument.setDocumentType("identity_recto");
                identityRectoDocument.setUser(car.getUser());
                car.getUser().getDocuments().add(identityRectoDocument);
            }

            if (carDTO.getIdentityVerso() != null && !carDTO.getIdentityVerso().isEmpty()) {
                String identityVersoPath = fileStorageService.storeFile(carDTO.getIdentityVerso(), "identity");
                Document identityVersoDocument = new Document();
                identityVersoDocument.setUrl(identityVersoPath);
                identityVersoDocument.setDocumentType("identity_verso");
                identityVersoDocument.setUser(car.getUser());
                car.getUser().getDocuments().add(identityVersoDocument);
            }

            if (carDTO.getIndisponibleDatesStart() != null && !carDTO.getIndisponibleDatesStart().isEmpty()) {
                List<Indisponible> periods = new ArrayList<>();
                List<String> startDates = carDTO.getIndisponibleDatesStart();
                List<String> endDates = carDTO.getIndisponibleDatesEnd();

                for (int i = 0; i < startDates.size(); i++) {
                    Indisponible indisponible = new Indisponible();
                    indisponible.setCar(car);
                    indisponible.setStartDate(LocalDate.parse(startDates.get(i)));
                    indisponible.setEndDate(LocalDate.parse(endDates.get(i)));
                    periods.add(indisponible);
                }
                car.setUnavailable(periods);
            }

            // Sauvegarder la voiture une seule fois à la fin
            return carRepository.save(car);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create car: " + e.getMessage(), e);
        }
    }

    private List<Feature> getFeatures(CarDTO carDTO) {
        List<Feature> features = new ArrayList<>();
        if (carDTO.getBoiteId() != null) {
            Feature boite = featureRepository.findById(carDTO.getBoiteId()).orElse(null);
            if (boite != null) {
                features.add(boite);
            }
        }

        if (carDTO.getCompteurId() != null) {
            Feature compteur = featureRepository.findById(carDTO.getCompteurId()).orElse(null);
            if (compteur != null) {
                features.add(compteur);
            }
        }

        if (carDTO.getPlacesId() != null) {
            Feature places = featureRepository.findById(carDTO.getPlacesId()).orElse(null);
            if (places != null) {
                features.add(places);
            }
        }

        if (carDTO.getPortesId() != null) {
            Feature portes = featureRepository.findById(carDTO.getPortesId()).orElse(null);
            if (portes != null) {
                features.add(portes);
            }
        }

        return features;
    }

    private List<Equipment> getEquipments(CarDTO carDTO) {
        List<Equipment> equipments = new ArrayList<>();
        if (carDTO.getEquipmentIds() != null) {
            for (Long equipmentId : carDTO.getEquipmentIds()) {
                Equipment equipment = equipmentService.getEquipmentById(equipmentId);
                if (equipment != null) {
                    equipments.add(equipment);
                }
            }
        }
        return equipments;
    }

    private List<Condition> getConditions(CarDTO carDTO, Car car) {
        List<Condition> conditions = new ArrayList<>();
        if (carDTO.getConditions() != null) {
            for (String conditionText : carDTO.getConditions()) {
                Condition condition = new Condition();
                condition.setCondition(conditionText);
                condition.setCar(car);
                conditions.add(condition);
            }
        }
        return conditions;
    }



    @Override
    public void updateCar(Long id, CarDTO carDTO) {
        Optional<Car> carOptional = carRepository.findById(id);
        if (carOptional.isPresent()) {
            Car car = carOptional.get();

            // Mettez à jour les propriétés de la voiture avec les données de CarDTO
            car.setBrand(carDTO.getBrand());
            car.setModel(carDTO.getModel());
            car.setFuelType(carDTO.getFuelType());
            car.setAdresse(carDTO.getAdresse());
            car.setCodePostal(carDTO.getCodePostal());
            car.setLocality(carDTO.getLocality());
            car.setPlaqueImmatriculation(carDTO.getPlaqueImmatriculation());
            car.setFirstImmatriculation(carDTO.getFirstImmatriculation());
            car.setCategory(categoryRepository.findById(carDTO.getCategoryId()).orElse(null));

            // Mettez à jour les identifiants des caractéristiques
            List<Feature> features = car.getFeatures();
            features.clear();

            Feature boite = featureRepository.findById(carDTO.getBoiteId()).orElse(null);
            if (boite != null) features.add(boite);

            Feature compteur = featureRepository.findById(carDTO.getCompteurId()).orElse(null);
            if (compteur != null) features.add(compteur);

            Feature places = featureRepository.findById(carDTO.getPlacesId()).orElse(null);
            if (places != null) features.add(places);

            Feature portes = featureRepository.findById(carDTO.getPortesId()).orElse(null);
            if (portes != null) features.add(portes);

            car.setFeatures(features);

            // Mettez à jour les équipements
            List<Equipment> equipments = equipmentService.getEquipmentByIds(carDTO.getEquipmentIds());
            car.setEquipments(equipments);

            // Enregistrez les mises à jour
            carRepository.save(car);
        } else {
            throw new EntityNotFoundException("Car not found with id " + carDTO.getId());
        }
    }


    public void updateCarCoordinates() {
        List<Car> cars = carRepository.findAll();
        for (Car car : cars) {
            String fullAddress = car.getAdresse() + ", " + car.getCodePostal() + " " + car.getLocality();
            GeocodingResult result = geocodingService.geocode(fullAddress);
            if (result != null) {
                car.setLatitude(result.geometry.location.lat);
                car.setLongitude(result.geometry.location.lng);
                carRepository.save(car);
            }
        }
    }

}






