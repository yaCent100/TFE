package be.iccbxl.tfe.Driveshare.restController;

import be.iccbxl.tfe.Driveshare.DTO.CarDTO;
import be.iccbxl.tfe.Driveshare.controller.CarController;
import be.iccbxl.tfe.Driveshare.model.Car;
import be.iccbxl.tfe.Driveshare.service.serviceImpl.CarService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
public class MapRestController {

    private final CarService carService;

    private static final Logger logger = LoggerFactory.getLogger(CarController.class);


    @Autowired
    public MapRestController(CarService carService) {
        this.carService = carService;
    }



    @GetMapping("/cars/addresses")
    public List<CarDTO> getAllCarAddresses() {
        return carService.getAllCars().stream()
                .map(this::convertToCarDTO)
                .collect(Collectors.toList());
    }



    // Méthode utilitaire pour convertir un objet Car en objet CarDTO
    private CarDTO convertToCarDTO(Car car) {
        CarDTO carDTO = new CarDTO(car);
        carDTO.setId(car.getId());
        carDTO.setAdresse(car.getAdresse());
        carDTO.setCodePostal(car.getCodePostal()); // Convertir en String si nécessaire
        carDTO.setLocality(car.getLocality());
        carDTO.setBrand(car.getBrand());
        carDTO.setModel(car.getModel());
        return carDTO;
    }

}
