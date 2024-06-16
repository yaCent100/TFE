package be.iccbxl.tfe.Driveshare.service.serviceImpl;

import be.iccbxl.tfe.Driveshare.DTO.CarDTO;
import be.iccbxl.tfe.Driveshare.model.*;
import be.iccbxl.tfe.Driveshare.repository.*;
import be.iccbxl.tfe.Driveshare.service.CarServiceI;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
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
    public List<Car> searchAvailableCars(String address, LocalDate dateDebut, LocalDate dateFin) {
        // Récupérer toutes les voitures
        List<Car> allCars = (List<Car>) carRepository.findAll();

        // Filtrer les voitures disponibles pour l'adresse spécifiée
        List<Car> availableCars = allCars.stream()
                .filter(car -> car.getAdresse().equalsIgnoreCase(address))
                .collect(Collectors.toList());

        // Filtrer les voitures disponibles pour la période de temps spécifiée
        availableCars = availableCars.stream()
                .filter(car -> isCarAvailableForDates(car, dateDebut, dateFin))
                .collect(Collectors.toList());

        return availableCars;
    }



    public boolean isCarAvailableForDates(Car car, LocalDate dateDebut, LocalDate dateFin) {
        // Récupérer toutes les réservations de la voiture
        List<CarRental> carRentals = car.getCarRentals();

        // Parcourir chaque location de voiture (CarRental) pour vérifier les dates de réservation
        for (CarRental carRental : carRentals) {
            List<Reservation> reservations = carRental.getReservations();

            // Vérifier chaque réservation pour un chevauchement avec les dates spécifiées
            for (Reservation reservation : reservations) {
                LocalDate reservationStartDate = reservation.getDebutLocation();
                LocalDate reservationEndDate = reservation.getFinLocation();

                // Vérifier s'il y a un chevauchement entre les dates de réservation et les dates spécifiées
                if ((dateDebut.isEqual(reservationStartDate) || dateDebut.isAfter(reservationStartDate)) &&
                        (dateDebut.isEqual(reservationEndDate) || dateDebut.isBefore(reservationEndDate)) ||
                        (dateFin.isEqual(reservationStartDate) || dateFin.isAfter(reservationStartDate)) &&
                                (dateFin.isEqual(reservationEndDate) || dateFin.isBefore(reservationEndDate))) {
                    // Il y a un chevauchement de dates, la voiture n'est pas disponible
                    return false;
                }
            }
        }

        // Aucun chevauchement trouvé, la voiture est disponible
        return true;
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

            Price price = configurePrice(carDTO, car);
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
                    indisponible.setDateDebut(LocalDate.parse(startDates.get(i)));
                    indisponible.setDateFin(LocalDate.parse(endDates.get(i)));
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

    private Price configurePrice(CarDTO carDTO, Car car) {
        Price price = new Price();
        price.setLowPrice(carDTO.getLowPrice());
        price.setMiddlePrice(carDTO.getMiddlePrice());
        price.setHighPrice(carDTO.getHighPrice());
        price.setPromo1(carDTO.getPromo1());
        price.setPromo2(carDTO.getPromo2());

        boolean isPromotionActive = (carDTO.getPromo1() != null && carDTO.getPromo1() > 0) ||
                (carDTO.getPromo2() != null && carDTO.getPromo2() > 0);
        price.setIsPromotion(isPromotionActive);

        price.setCar(car);
        return price;
    }



}






