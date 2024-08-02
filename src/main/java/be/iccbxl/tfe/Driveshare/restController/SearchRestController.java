package be.iccbxl.tfe.Driveshare.restController;

import be.iccbxl.tfe.Driveshare.DTO.CarDTO;
import be.iccbxl.tfe.Driveshare.service.serviceImpl.CarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class SearchRestController {

    @Autowired
    private CarService carService;

    @GetMapping("api/cars/search")
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



}
