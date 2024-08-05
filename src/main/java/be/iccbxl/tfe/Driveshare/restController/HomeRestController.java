package be.iccbxl.tfe.Driveshare.restController;

import be.iccbxl.tfe.Driveshare.DTO.CarDTO;
import be.iccbxl.tfe.Driveshare.model.Car;
import be.iccbxl.tfe.Driveshare.service.serviceImpl.CarService;
import be.iccbxl.tfe.Driveshare.service.serviceImpl.EvaluationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class HomeRestController {

    @Autowired
    private CarService carService;
    @Autowired
    private EvaluationService evaluationService;



    @PostMapping("/api/cars/search/category")
    public List<CarDTO> searchCars(@RequestBody Car searchRequest) {
        return carService.searchCarsByCategory(searchRequest.getCategory().getCategory());
    }

    @GetMapping("/api/cars/top-rated")
    public List<CarDTO> getTopRatedCars() {
        return evaluationService.getTop4CarsWithFiveStarRating();
    }
}
