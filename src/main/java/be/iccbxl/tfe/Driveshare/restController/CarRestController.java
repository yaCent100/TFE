package be.iccbxl.tfe.Driveshare.restController;

import be.iccbxl.tfe.Driveshare.DTO.CarDTO;
import be.iccbxl.tfe.Driveshare.DTO.CategoryDTO;
import be.iccbxl.tfe.Driveshare.DTO.MapperDTO;
import be.iccbxl.tfe.Driveshare.model.Car;
import be.iccbxl.tfe.Driveshare.model.Category;
import be.iccbxl.tfe.Driveshare.service.serviceImpl.CarService;
import be.iccbxl.tfe.Driveshare.service.serviceImpl.CategoryService;
import be.iccbxl.tfe.Driveshare.service.serviceImpl.FeatureService;
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
public class CarRestController {

    @Autowired
    private CarService carService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private FeatureService featureService;

    @GetMapping("/api/cars")
        public ResponseEntity<Map<String, Object>> getAllCars() {
            List<Car> cars = carService.getAllCars();
            List<Category> categories = categoryService.getAllCategory();
            Map<Long, Double> averageRatings = carService.getAverageRatingsForCars();
            Map<Long, Integer> reviewCounts = carService.getReviewCountsForCars();

            List<CarDTO> carDTOs = cars.stream().map(car -> {
                CarDTO carDTO = MapperDTO.toCarDTO(car);
                return carDTO;
            }).collect(Collectors.toList());

            List<CategoryDTO> categoryDTOs = categories.stream()
                    .map(category -> new CategoryDTO(category.getId(), category.getCategory()))
                    .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("cars", carDTOs);
            response.put("categories", categoryDTOs);
            response.put("averageRatings", averageRatings);
            response.put("reviewCounts", reviewCounts);

            return new ResponseEntity<>(response, HttpStatus.OK);
        }


    @GetMapping("/api/cars/search")
    public ResponseEntity<List<Map<String, Object>>> searchCars(
            @RequestParam String address,
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin) throws Exception {

        List<CarDTO> availableCars = carService.searchAvailableCars(address, lat, lng, dateDebut, dateFin);

        Map<Long, Double> averageRatings = carService.getAverageRatingsForCars();
        Map<Long, Integer> reviewCounts = carService.getReviewCountsForCars();

        List<Map<String, Object>> carsWithRatings = new ArrayList<>();

        for (CarDTO car : availableCars) {
            Map<String, Object> carMap = new HashMap<>();
            carMap.put("car", car);

            double averageRating = averageRatings.getOrDefault(car.getId(), 0.0);
            int reviewCount = reviewCounts.getOrDefault(car.getId(), 0);

            carMap.put("averageRating", averageRating);
            carMap.put("reviewCount", reviewCount);

            carsWithRatings.add(carMap);
        }

        return ResponseEntity.ok(carsWithRatings);
    }



    @GetMapping("/api/categories")
    public List<Category> getAllCategories() {
        return categoryService.getAllCategory();
    }

    @GetMapping("/api/gearbox")
    public ResponseEntity<List<String>> getGearboxTypes() {
        List<String> gearboxes = featureService.getAllGearboxTypes();
        return new ResponseEntity<>(gearboxes, HttpStatus.OK);
    }

    @GetMapping("/api/motorisation")
    public ResponseEntity<List<String>> getMotorisationTypes() {
        List<String> motorisations = featureService.getAllMotorisationTypes();
        return new ResponseEntity<>(motorisations, HttpStatus.OK);
    }

    @GetMapping("/api/kilometrage")
    public ResponseEntity<List<String>> getKilometrageOptions() {
        List<String> kilometrages = featureService.getAllKilometrageOptions();
        return new ResponseEntity<>(kilometrages, HttpStatus.OK);
    }

    @GetMapping("/api/places")
    public ResponseEntity<List<String>> getPlacesOptions() {
        List<String> places = featureService.getAllPlacesOptions();
        return new ResponseEntity<>(places, HttpStatus.OK);
    }




}
