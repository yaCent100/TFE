package be.iccbxl.tfe.Driveshare.restController;

import be.iccbxl.tfe.Driveshare.DTO.CarDTO;
import be.iccbxl.tfe.Driveshare.DTO.CategoryDTO;
import be.iccbxl.tfe.Driveshare.DTO.MapperDTO;
import be.iccbxl.tfe.Driveshare.model.Car;
import be.iccbxl.tfe.Driveshare.model.Category;
import be.iccbxl.tfe.Driveshare.service.serviceImpl.CarService;
import be.iccbxl.tfe.Driveshare.service.serviceImpl.CategoryService;
import be.iccbxl.tfe.Driveshare.service.serviceImpl.FeatureService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    @Operation(summary = "Obtenir toutes les voitures", description = "Retourne la liste de toutes les voitures disponibles, avec les catégories, notes moyennes et nombre d'avis.")
    @GetMapping("/api/cars")
    public ResponseEntity<Map<String, Object>> getAllCars() {
        List<Car> cars = carService.getAllCars();
        List<Category> categories = categoryService.getAllCategory();
        Map<Long, Double> averageRatings = carService.getAverageRatingsForCars();
        Map<Long, Integer> reviewCounts = carService.getReviewCountsForCars();

        List<CarDTO> carDTOs = cars.stream().map(MapperDTO::toCarDTO).collect(Collectors.toList());
        List<CategoryDTO> categoryDTOs = categories.stream().map(category -> new CategoryDTO(category.getId(), category.getCategory())).collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("cars", carDTOs);
        response.put("categories", categoryDTOs);
        response.put("averageRatings", averageRatings);
        response.put("reviewCounts", reviewCounts);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Operation(summary = "Rechercher des voitures disponibles", description = "Recherche des voitures disponibles en fonction de l'adresse, de la localisation et des dates de location.")
    @GetMapping("/api/cars/search")
    public ResponseEntity<List<Map<String, Object>>> searchCars(
            @Parameter(description = "L'adresse pour rechercher les voitures", required = true) @RequestParam String address,
            @Parameter(description = "Latitude de l'emplacement", required = true) @RequestParam double lat,
            @Parameter(description = "Longitude de l'emplacement", required = true) @RequestParam double lng,
            @Parameter(description = "Date de début de location", required = true) @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
            @Parameter(description = "Date de fin de location", required = true) @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin) throws Exception {

        List<CarDTO> availableCars = carService.searchAvailableCars(address, lat, lng, dateDebut, dateFin);
        Map<Long, Double> averageRatings = carService.getAverageRatingsForCars();
        Map<Long, Integer> reviewCounts = carService.getReviewCountsForCars();

        List<Map<String, Object>> carsWithRatings = new ArrayList<>();
        for (CarDTO car : availableCars) {
            Map<String, Object> carMap = new HashMap<>();
            carMap.put("car", car);
            carMap.put("averageRating", averageRatings.getOrDefault(car.getId(), 0.0));
            carMap.put("reviewCount", reviewCounts.getOrDefault(car.getId(), 0));
            carsWithRatings.add(carMap);
        }

        return ResponseEntity.ok(carsWithRatings);
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

