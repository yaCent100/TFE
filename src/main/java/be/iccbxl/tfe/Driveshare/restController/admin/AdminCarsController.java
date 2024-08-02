package be.iccbxl.tfe.Driveshare.restController.admin;

import be.iccbxl.tfe.Driveshare.service.serviceImpl.CarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminCarsController {

    @Autowired
    private CarService carService;

    @PostMapping("/car/approve/{id}")
    public ResponseEntity<String> approveCar(@PathVariable Long id) {
        boolean success = carService.approveCar(id);
        if (success) {
            return ResponseEntity.ok("Car approved successfully");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Car not found");
        }
    }
}
