package be.iccbxl.tfe.Driveshare.controller;

import be.iccbxl.tfe.Driveshare.classes.CarDTO;
import be.iccbxl.tfe.Driveshare.service.serviceImpl.CarService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
public class MapController {

    private final CarService carService;

    public MapController(CarService carService) {
        this.carService = carService;
    }

    @GetMapping("/cars/addresses")
    public List<CarDTO> getAllCarAddresses() {
        return carService.getAllCars().stream()
                .map(car -> {
                    CarDTO carDTO = new CarDTO();
                    carDTO.setId(car.getId());
                    carDTO.setAdresse(car.getAdresse());
                    carDTO.setCodePostal(Integer.toString(car.getCodePostal())); // Convertir en String
                    carDTO.setLocality(car.getLocality());
                    carDTO.setBrand(car.getBrand());
                    carDTO.setModel(car.getModel());
                    return carDTO;
                })
                .collect(Collectors.toList());
    }



}
