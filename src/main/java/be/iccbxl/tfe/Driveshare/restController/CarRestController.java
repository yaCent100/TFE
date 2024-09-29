package be.iccbxl.tfe.Driveshare.restController;

import be.iccbxl.tfe.Driveshare.DTO.CarDTO;
import be.iccbxl.tfe.Driveshare.DTO.CarDTOHome;
import be.iccbxl.tfe.Driveshare.DTO.CategoryDTO;
import be.iccbxl.tfe.Driveshare.DTO.MapperDTO;
import be.iccbxl.tfe.Driveshare.model.Car;
import be.iccbxl.tfe.Driveshare.model.Category;
import be.iccbxl.tfe.Driveshare.service.serviceImpl.CarService;
import be.iccbxl.tfe.Driveshare.service.serviceImpl.CategoryService;
import be.iccbxl.tfe.Driveshare.service.serviceImpl.FeatureService;
import be.iccbxl.tfe.Driveshare.service.serviceImpl.PriceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@Tag(name = "Car Management", description = "Gestion des voitures et recherche de voitures")
public class CarRestController {

    @Autowired
    private CarService carService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private FeatureService featureService;

    @Autowired
    private PriceService priceService;

    private static final Logger logger = LoggerFactory.getLogger(CarRestController.class);

    @GetMapping("/api/cars")
    public ResponseEntity<Map<String, Object>> getAllOnlineCars(@RequestParam int page, @RequestParam int limit) {
        // Créer une instance de PageRequest avec la page et la limite spécifiées
        PageRequest pageRequest = PageRequest.of(page - 1, limit);

        // Récupérer les voitures en ligne avec pagination
        Page<Car> carsPage = carService.getAllOnlineCars(pageRequest);

        // Récupérer les notes moyennes et les nombres d'avis pour toutes les voitures en une seule fois
        Map<Long, Double> averageRatings = carService.getAverageRatingsForCars();
        Map<Long, Integer> reviewCounts = carService.getReviewCountsForCars();

        // Transformer les voitures en CarDTO
        List<CarDTO> carDTOs = carsPage.getContent().stream().map(car -> {
            double averageRating = averageRatings.getOrDefault(car.getId(), 0.0);
            int reviewCount = reviewCounts.getOrDefault(car.getId(), 0);

            // Calculer le displayPrice
            double displayPrice = car.getPrice() != null
                    ? priceService.calculateDisplayPrice(car.getPrice(), LocalDate.now())
                    : 0.0;

            // Créer le DTO en incluant la note moyenne, le nombre d'avis et le displayPrice
            return new CarDTO(car, averageRating, reviewCount, 0.0, displayPrice);  // `distance` est défini à 0.0 car non pertinent ici
        }).collect(Collectors.toList());

        // Préparer la réponse
        Map<String, Object> response = new HashMap<>();
        response.put("cars", carDTOs);
        response.put("currentPage", carsPage.getNumber() + 1); // +1 car Spring commence à 0
        response.put("totalItems", carsPage.getTotalElements());
        response.put("totalPages", carsPage.getTotalPages());

        return new ResponseEntity<>(response, HttpStatus.OK);
    }




    @GetMapping("/api/cars/search")
    public ResponseEntity<Map<String, Object>> searchCars(
            @RequestParam String address,
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "0") int limit) {

        // Validation des paramètres
        if (page < 1 || limit < 1) {
            logger.error("Invalid page or limit: page = {}, limit = {}", page, limit);
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Page and limit must be greater than 0."));
        }

        logger.info("Searching cars with address: {}, lat: {}, lng: {}, dateDebut: {}, dateFin: {}, page: {}, limit: {}",
                address, lat, lng, dateDebut, dateFin, page, limit);

        PageRequest pageRequest = PageRequest.of(page - 1, limit);

        try {
            Page<CarDTO> carsPage = carService.searchAvailableCars(address, lat, lng, dateDebut, dateFin, pageRequest);

            logger.info("Found {} cars on page {}", carsPage.getTotalElements(), carsPage.getNumber() + 1);

            Map<Long, Double> averageRatings = carService.getAverageRatingsForCars();
            Map<Long, Integer> reviewCounts = carService.getReviewCountsForCars();

            List<Map<String, Object>> carsWithDetails = new ArrayList<>();

            for (CarDTO car : carsPage.getContent()) {
                Map<String, Object> carMap = new HashMap<>();
                carMap.put("car", car);
                carMap.put("averageRating", averageRatings.getOrDefault(car.getId(), 0.0));
                carMap.put("reviewCount", reviewCounts.getOrDefault(car.getId(), 0));
                carMap.put("distance", car.getDistance()); // Assurez-vous que `getDistance` est correctement implémenté

                logger.debug("Car ID: {}, Distance: {}", car.getId(), car.getDistance());

                carsWithDetails.add(carMap);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("totalCars", carsPage.getTotalElements());
            response.put("totalPages", carsPage.getTotalPages());
            response.put("currentPage", carsPage.getNumber() + 1);
            response.put("cars", carsWithDetails);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error occurred while searching cars", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "Une erreur est survenue lors de la recherche des voitures."));
        }
    }


    @GetMapping("/api/cars/{id}")
    @ResponseBody
    public ResponseEntity<CarDTOHome> getCarById(@PathVariable Long id) {
        Car car = carService.getCarById(id);
        if (car == null) {
            return ResponseEntity.notFound().build();
        }

        // Création directe de l'objet CarDTOHome
        CarDTOHome carDTOHome = new CarDTOHome(
                car.getId(),
                car.getBrand(),
                car.getModel(),
                car.getAdresse(),
                car.getPostalCode(),
                car.getLocality()
        );

        return ResponseEntity.ok(carDTOHome);
    }





    @Operation(summary = "Obtenir toutes les catégories", description = "Retourne la liste de toutes les catégories de voitures disponibles.")
    @GetMapping("/api/categories")
    public List<CategoryDTO> getAllCategories() {
        List<Category> categories = categoryService.getAllCategory();
        return categories.stream()
                .map(MapperDTO::toCategoryDTO)
                .collect(Collectors.toList());
    }
    @Operation(summary = "Obtenir les types de boîte de vitesses", description = "Retourne la liste des types de boîte de vitesses disponibles.")
    @GetMapping("/api/gearbox")
    public ResponseEntity<List<String>> getGearboxTypes() {
        return new ResponseEntity<>(featureService.getAllGearboxTypes(), HttpStatus.OK);
    }

    @Operation(summary = "Obtenir les types de motorisation", description = "Retourne la liste des types de motorisation disponibles.")
    @GetMapping("/api/motorisation")
    public ResponseEntity<List<String>> getMotorisationTypes() {
        return new ResponseEntity<>(featureService.getAllMotorisationTypes(), HttpStatus.OK);
    }

    @Operation(summary = "Obtenir les options de kilométrage", description = "Retourne la liste des options de kilométrage disponibles.")
    @GetMapping("/api/kilometrage")
    public ResponseEntity<List<String>> getKilometrageOptions() {
        return new ResponseEntity<>(featureService.getAllKilometrageOptions(), HttpStatus.OK);
    }

    @Operation(summary = "Obtenir les options de places", description = "Retourne la liste des options de places disponibles pour les voitures.")
    @GetMapping("/api/places")
    public ResponseEntity<List<String>> getPlacesOptions() {
        return new ResponseEntity<>(featureService.getAllPlacesOptions(), HttpStatus.OK);
    }
}

