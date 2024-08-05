package be.iccbxl.tfe.Driveshare.restController.admin;

import be.iccbxl.tfe.Driveshare.DTO.CarDTO;
import be.iccbxl.tfe.Driveshare.DTO.CarMapper;
import be.iccbxl.tfe.Driveshare.model.Car;
import be.iccbxl.tfe.Driveshare.service.serviceImpl.CarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/cars")
public class AdminCarsRestController {
    @Autowired
    private CarService carService;


    @GetMapping("/online")
    public List<CarDTO> getOnlineCars() {
        return carService.getOnlineCars();
    }

    @GetMapping("/pending")
    public List<CarDTO> getPendingCars() {
        return carService.getPendingCars();
    }

    @GetMapping("/{id}")
    public ResponseEntity<CarDTO> getCarById(@PathVariable Long id) {
        Car car = carService.getCarById(id);
        if (car != null) {
            CarDTO carDTO = CarMapper.toCarDTO(car);
            return ResponseEntity.ok(carDTO);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @PostMapping("/approve/{id}")
    public ResponseEntity<CarDTO> approveCar(@PathVariable Long id) {
        CarDTO approvedCar = carService.approveCar(id);
        if (approvedCar != null) {
            return ResponseEntity.ok(approvedCar);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }
}
