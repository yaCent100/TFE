package be.iccbxl.tfe.Driveshare.controller;

import be.iccbxl.tfe.Driveshare.DTO.CarDTO;
import be.iccbxl.tfe.Driveshare.model.Car;
import be.iccbxl.tfe.Driveshare.service.serviceImpl.CarService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class MapRestController {

    private final CarService carService;

    public MapRestController(CarService carService) {
        this.carService = carService;
    }

    @GetMapping("/cars/addresses")
    public List<CarDTO> getAllCarAddresses() {
        return carService.getAllCars().stream()
                .map(this::convertToCarDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/search")
    public ResponseEntity<List<CarDTO>> searchAvailableCars(@RequestParam String address,
                                                            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
                                                            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin) {
        // Recherchez les voitures disponibles pour les paramètres de recherche
        List<CarDTO> availableCars = carService.searchAvailableCars(address, dateDebut, dateFin)
                .stream()
                .map(this::convertToCarDTO)
                .collect(Collectors.toList());

        // Vérifiez si des voitures sont disponibles
        if (availableCars.isEmpty()) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.ok(availableCars);
        }
    }


        // Méthode utilitaire pour convertir un objet Car en objet CarDTO
    private CarDTO convertToCarDTO(Car car) {
        CarDTO carDTO = new CarDTO();
        carDTO.setId(car.getId());
        carDTO.setAdresse(car.getAdresse());
        carDTO.setCodePostal(car.getCodePostal()); // Convertir en String si nécessaire
        carDTO.setLocality(car.getLocality());
        carDTO.setBrand(car.getBrand());
        carDTO.setModel(car.getModel());
        return carDTO;
    }
}
