package be.iccbxl.tfe.Driveshare.service.serviceImpl;

import be.iccbxl.tfe.Driveshare.DTO.CarDTO;
import be.iccbxl.tfe.Driveshare.DTO.MapperDTO;
import be.iccbxl.tfe.Driveshare.classes.CustomGeocodingResult;
import be.iccbxl.tfe.Driveshare.model.*;
import be.iccbxl.tfe.Driveshare.repository.*;
import be.iccbxl.tfe.Driveshare.service.CarServiceI;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springdoc.api.OpenApiResourceNotFoundException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
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
    private MapboxGeocodingService geocodingService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PriceService priceService;

    @Autowired
    private ReservationRepository reservationRepository;

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
    public Car updateCarPhoto(Long carId, String photoUrl) {
        // Vérifie si la voiture existe dans la base de données
        Optional<Car> existingCar = carRepository.findById(carId);

        if (existingCar.isPresent()) {
            Car car = existingCar.get();

            // Récupérer la liste actuelle des photos associées à cette voiture
            List<Photo> photos = car.getPhotos();

            // Créer un nouvel objet Photo et lui assigner l'URL de la nouvelle photo
            Photo newPhoto = new Photo();
            newPhoto.setUrl(photoUrl);
            newPhoto.setCar(car);  // Associe la nouvelle photo à la voiture

            // Ajouter la nouvelle photo à la liste des photos de la voiture
            photos.add(newPhoto);

            // Mettre à jour la liste des photos de la voiture
            car.setPhotos(photos);

            // Sauvegarder la voiture mise à jour dans la base de données
            return carRepository.save(car);  // carRepository va gérer l'enregistrement de la voiture et des nouvelles photos
        } else {
            throw new OpenApiResourceNotFoundException("Car with id " + carId + " not found");
        }
    }



    @Transactional
    public void deleteCar(Long id) {
        // Supprimer la voiture par ID
        carRepository.deleteCarById(id);
        System.out.println("Car deleted: " + id);
    }



    public List<CarDTO> searchCarsByCategory(String category) {
        List<Car> cars = carRepository.findByCategory_Category(category);
        return cars.stream().map(MapperDTO::toCarDTO).collect(Collectors.toList());
    }

    @Override
    public List<Car> getCarsByUser(User user) {
        return carRepository.findByUser(user);
    }

    @Override
    public double calculateAverageRating(Car car) {
        List<Reservation> reservations = car.getReservations();
        if (reservations == null || reservations.isEmpty()) {
            return 0.0; // Retourne 0 si aucune réservation n'est associée à la voiture
        }

        double sum = 0.0;
        int totalEvaluations = 0; // Compte le nombre total d'évaluations pour faire la moyenne

        // Parcourir chaque réservation de la voiture
        for (Reservation reservation : reservations) {
            Evaluation evaluation = reservation.getEvaluation(); // Obtenir l'évaluation de chaque réservation
            if (evaluation != null) {
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
            List<Reservation> reservations = car.getReservations(); // Obtenir toutes les réservations de la voiture
            int count = 0;

            for (Reservation reservation : reservations) {
                Evaluation evaluation = reservation.getEvaluation();
                if (evaluation != null) {
                    count++; // Incrémenter le compteur si une évaluation est présente pour cette réservation
                }
            }

            reviewCounts.put(car.getId(), count); // Stocker le nombre total d'évaluations pour cette voiture
        }

        return reviewCounts;
    }


    @Override
    @Transactional(readOnly = true)
    public Page<CarDTO> searchAvailableCars(String address, double lat, double lng, LocalDate dateDebut, LocalDate dateFin, PageRequest pageRequest) throws Exception {
        CustomGeocodingResult userLocation = geocodingService.geocodeAddress(address);

        if (userLocation == null) {
            throw new Exception("Unable to geocode the provided address.");
        }

        double userLat = userLocation.getLatitude();
        double userLng = userLocation.getLongitude();
        logger.info("User Location: Lat: {}, Lng: {}", userLat, userLng);

        List<Car> allCars = carRepository.findAll();
        logger.info("Total cars retrieved from DB: {}", allCars.size());

        Map<Long, Double> averageRatings = getAverageRatingsForCars();
        Map<Long, Integer> reviewCounts = getReviewCountsForCars();

        List<CarDTO> availableCars = allCars.stream()
                .filter(car -> isCarAvailableForDates(car, dateDebut, dateFin))
                .filter(car -> isCarNearby(car, userLat, userLng, 5.0))  // Utiliser le seuil de distance de 5.0 km ici
                .map(car -> {
                    double distance = -1;
                    if (car.getLatitude() != null && car.getLongitude() != null) {
                        distance = calculateHaversineDistance(userLat, userLng, car.getLatitude(), car.getLongitude());
                        distance = Math.round(distance * 100.0) / 100.0;
                    }

                    double displayPrice = 0.0;
                    if (car.getPrice() != null) {
                        displayPrice = priceService.calculateDisplayPrice(car.getPrice(), LocalDate.now());
                    }

                    CarDTO carDTO = new CarDTO(car, averageRatings.getOrDefault(car.getId(), 0.0), reviewCounts.getOrDefault(car.getId(), 0), distance, displayPrice);

                    return carDTO;
                })
                .sorted(Comparator.comparingDouble(CarDTO::getDistance))
                .collect(Collectors.toList());

        int startIndex = pageRequest.getPageNumber() * pageRequest.getPageSize();
        int endIndex = Math.min(startIndex + pageRequest.getPageSize(), availableCars.size());

        if (startIndex >= availableCars.size()) {
            return new PageImpl<>(Collections.emptyList(), pageRequest, availableCars.size());
        }

        List<CarDTO> pageContent = availableCars.subList(startIndex, endIndex);
        return new PageImpl<>(pageContent, pageRequest, availableCars.size());
    }




    public boolean isCarAvailableForDates(Car car, LocalDate dateDebut, LocalDate dateFin) {
        List<Reservation> reservations = car.getReservations();
        List<Indisponible> unavailabilities = car.getUnavailable();

        // Liste des statuts qui bloquent la disponibilité de la voiture
        List<String> blockingStatuses = Arrays.asList("NOW", "CONFIRMED", "PAYMENT_PENDING", "RESPONSE_PENDING");

        // Vérification des réservations
        for (Reservation reservation : reservations) {
            LocalDate reservationStartDate = reservation.getStartLocation();
            LocalDate reservationEndDate = reservation.getEndLocation();

            // Si le statut de la réservation actuelle est dans la liste des statuts bloquants
            if (blockingStatuses.contains(reservation.getStatut())) {
                // Vérifier si les dates se chevauchent
                if ((dateDebut.isEqual(reservationStartDate) || dateDebut.isAfter(reservationStartDate)) &&
                        (dateDebut.isEqual(reservationEndDate) || dateDebut.isBefore(reservationEndDate)) ||
                        (dateFin.isEqual(reservationStartDate) || dateFin.isAfter(reservationStartDate)) &&
                                (dateFin.isEqual(reservationEndDate) || dateFin.isBefore(reservationEndDate))) {
                    return false; // La voiture n'est pas disponible
                }
            }
        }

        // Vérification des périodes d'indisponibilité
        for (Indisponible unavailability : unavailabilities) {
            LocalDate unavailabilityStartDate = unavailability.getStartDate();
            LocalDate unavailabilityEndDate = unavailability.getEndDate();

            // Vérifier si les dates se chevauchent avec une période d'indisponibilité
            if ((dateDebut.isEqual(unavailabilityStartDate) || dateDebut.isAfter(unavailabilityStartDate)) &&
                    (dateDebut.isEqual(unavailabilityEndDate) || dateDebut.isBefore(unavailabilityEndDate)) ||
                    (dateFin.isEqual(unavailabilityStartDate) || dateFin.isAfter(unavailabilityStartDate)) &&
                            (dateFin.isEqual(unavailabilityEndDate) || dateFin.isBefore(unavailabilityEndDate))) {
                return false; // La voiture est indisponible à ces dates
            }
        }

        return true; // La voiture est disponible
    }



    private boolean isCarNearby(Car car, double userLat, double userLng, double distanceThreshold) {
        Double carLatDouble = car.getLatitude();
        Double carLngDouble = car.getLongitude();

        // Vérifier si la latitude ou la longitude est null
        if (carLatDouble == null || carLngDouble == null) {
            logger.warn("Car ID: {} has null coordinates, skipping distance check.", car.getId());
            return false;
        }

        // Convertir Double en double (type primitif)
        double carLat = carLatDouble;
        double carLng = carLngDouble;

        // Vérifier si les coordonnées sont par défaut
        if (carLat == 0.0 && carLng == 0.0) {
            logger.warn("Car ID: {} has default coordinates (0.0, 0.0), skipping distance check.", car.getId());
            return false;
        }

        // Calculer la distance
        double distance = calculateHaversineDistance(userLat, userLng, carLat, carLng);

        // Loger les informations de la voiture, qu'elle soit proche ou pas
        logger.info("Car ID: {}, Car Lat: {}, Car Lng: {}, Distance: {}", car.getId(), carLat, carLng, distance);

        // Retourner si la distance est dans le seuil
        return distance <= distanceThreshold;
    }


    public double calculateHaversineDistance(double lat1, double lng1, double lat2, double lng2) {
        logger.info("Calculating distance between Origin (lat: {}, lng: {}) and Car (lat: {}, lng: {})", lat1, lng1, lat2, lng2);

        final int EARTH_RADIUS = 6371; // Rayon de la Terre en kilomètres
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = EARTH_RADIUS * c;

        logger.info("Calculated distance: {}", distance);
        return distance;
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

            Price price = MapperDTO.toPrice(carDTO.getPrice()); // Conversion du DTO en entité Price
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
                String registrationCardPath = fileStorageService.storeFile(carDTO.getRegistrationCard(), "registrationCard");
                car.setCarteGrisePath(registrationCardPath);
            }

            if (carDTO.getIdentityRecto() != null && !carDTO.getIdentityRecto().isEmpty()) {
                String identityRectoPath = fileStorageService.storeFile(carDTO.getIdentityRecto(), "identityCard");
                Document identityRectoDocument = new Document();
                identityRectoDocument.setUrl(identityRectoPath);
                identityRectoDocument.setDocumentType("identity_recto");
                identityRectoDocument.setUser(car.getUser());
                car.getUser().getDocuments().add(identityRectoDocument);
            }

            if (carDTO.getIdentityVerso() != null && !carDTO.getIdentityVerso().isEmpty()) {
                String identityVersoPath = fileStorageService.storeFile(carDTO.getIdentityVerso(), "identityCard");
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
            car.setPostalCode(carDTO.getCodePostal());
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



    @Transactional
    public void updateCarCoordinates() throws Exception {
        List<Car> cars = carRepository.findAll();
        List<Car> updatedCars = new ArrayList<>();

        for (Car car : cars) {
            String fullAddress = car.getAdresse() + ", " + car.getPostalCode() + " " + car.getLocality() + ", Belgium";
            CustomGeocodingResult result = geocodingService.geocodeAddress(fullAddress);

            if (result != null) {
                car.setLatitude(result.getLatitude());
                car.setLongitude(result.getLongitude());
                updatedCars.add(car);  // Ajoutez la voiture à la liste des voitures mises à jour
            } else {
                logger.warn("Geocoding failed for address: {}", fullAddress);
            }
        }

        if (!updatedCars.isEmpty()) {
            carRepository.saveAll(updatedCars);  // Enregistrer toutes les voitures mises à jour en une seule opération
            logger.info("Updated coordinates for {} cars", updatedCars.size());
        } else {
            logger.info("No cars to update.");
        }
    }




   /* public List<Car> getPendingCars() {
        return carRepository.findByOnline(false);
    }*/

    public List<CarDTO> getAll() {
        List<Car> all = carRepository.findAll();
        return all.stream().map(MapperDTO::toCarDTO).collect(Collectors.toList());
    }

    public List<CarDTO> getPendingCars() {
        List<Car> pendingCars = carRepository.findByOnline(false);
        return pendingCars.stream().map(MapperDTO::toCarDTO).collect(Collectors.toList());
    }

    public CarDTO approveCar(Long id) {
        Car car = carRepository.findById(id).orElse(null);
        if (car != null) {
            car.setOnline(true); // Mise en ligne de la voiture
            Car updatedCar = carRepository.save(car);

            // Envoi d'un email à l'utilisateur pour l'informer que la voiture est désormais en ligne
            String userEmail = car.getUser().getEmail();
            String subject = "Votre voiture a été approuvée et est en ligne";
            String message = "Bonjour " + car.getUser().getFirstName() + ",\n\n" +
                    "Nous sommes heureux de vous informer que votre voiture " + car.getBrand() + " " + car.getModel() +
                    " a été approuvée et est maintenant disponible en ligne.\n\n" +
                    "Merci de faire confiance à notre service !\n" +
                    "Cordialement,\nL'équipe Driveshare";

            // Appel du service d'envoi d'email
            emailService.sendNotificationEmail(userEmail, subject, message);

            // Retourner l'objet DTO de la voiture approuvée
            return MapperDTO.toCarDTO(updatedCar);
        }
        return null; // Si la voiture n'existe pas
    }


    public boolean rejectCar(Long id) {
        Optional<Car> optionalCar = carRepository.findById(id);

        if (optionalCar.isPresent()) {
            try {
                Car car = optionalCar.get();
                carRepository.delete(car);

                // Envoyer un email à l'utilisateur pour l'informer du rejet de la voiture
                String userEmail = car.getUser().getEmail();
                String subject = "Votre voiture n'a pas été acceptée";
                String message = "Bonjour " + car.getUser().getFirstName() + ",\n\n" +
                        "Nous sommes désolés de vous informer que votre voiture " + car.getBrand() + " " + car.getModel() +
                        " n'a pas été acceptée. Pour plus de détails, veuillez nous contacter.\n\n" +
                        "Cordialement,\nL'équipe Driveshare";

                emailService.sendNotificationEmail(userEmail, subject, message);

                return true;
            } catch (Exception e) {
                // Gérer l'exception, comme enregistrer un message d'erreur dans les logs
                logger.error("Erreur lors de la suppression de la voiture avec ID " + id, e);
                return false;
            }
        }

        return false;
    }



    public List<Car> getTopRatedCars(int limit) {
        List<Car> allCars = (List<Car>) carRepository.findAll();
        List<Car> topRatedCars = new ArrayList<>();

        for (Car car : allCars) {
            double averageRating = getAverageRatingsForCars().get(car.getId());
            if (averageRating == 5.0) {
                topRatedCars.add(car);
            }
            if (topRatedCars.size() >= limit) {
                break;
            }
        }

        return topRatedCars;
    }


    public long countTotalCars() {
        return carRepository.count();
    }

    public long countOnlineCars() {
        return carRepository.countByOnline(true);
    }

    public long countCarsRented() {
        return reservationRepository.countByStatut("NOW"); // assuming "RENTED" indicates a car currently rented
    }

    public long countPendingCars() {
        return carRepository.countByOnline(false);
    }

    public List<Object[]> getReservationsByMonth() {
        return reservationRepository.countReservationsByMonth(); // This method needs to be created in the repo
    }


    public List<Car> getAllOnlineCars() {
        // Filtrer les voitures pour ne retourner que celles qui sont "online"
        return carRepository.findAll().stream()
                .filter(Car::getOnline) // Supposons que `isOnline` soit une méthode ou un champ booléen
                .collect(Collectors.toList());
    }

    public Page<Car> getAllOnlineCars(PageRequest pageRequest) {
        return carRepository.findAllByOnlineTrue(pageRequest);
    }


    @Transactional
    public Car updateCar(Car car) {
        return carRepository.save(car); // Enregistrer les modifications dans la base de données
    }


    public List<Object[]> getCarsByLocality() {
        return carRepository.findCarsByLocality();
    }
}






