package be.iccbxl.tfe.Driveshare.restController.admin;

import be.iccbxl.tfe.Driveshare.DTO.CarDTO;
import be.iccbxl.tfe.Driveshare.DTO.MapperDTO;
import be.iccbxl.tfe.Driveshare.model.Car;
import be.iccbxl.tfe.Driveshare.service.serviceImpl.CarService;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.models.annotations.OpenAPI30;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/cars")
@Tag(name = "Admin Car Management", description = "API pour la gestion des voitures par les administrateurs")
public class AdminCarsRestController {

    @Autowired
    private CarService carService;

    @Operation(summary = "Obtenir toutes les voitures en ligne", description = "Récupérer la liste de toutes les voitures actuellement en ligne.")
    @GetMapping("/online")
    public List<CarDTO> getOnlineCars() {
        return carService.getOnlineCars();
    }

    @Operation(summary = "Obtenir toutes les voitures en attente d'approbation", description = "Récupérer la liste de toutes les voitures en attente d'approbation.")
    @GetMapping("/pending")
    public List<CarDTO> getPendingCars() {
        return carService.getPendingCars();
    }

    @Operation(summary = "Obtenir une voiture par ID", description = "Récupérer les détails d'une voiture spécifique à partir de son identifiant.")
    @GetMapping("/{id}")
    public ResponseEntity<CarDTO> getCarById(
            @Parameter(description = "L'ID de la voiture à récupérer", required = true) @PathVariable Long id) {
        Car car = carService.getCarById(id);

        if (car != null) {
            CarDTO carDTO = MapperDTO.toCarDTO(car);
            carDTO.setCarteGrisePath("/api/files/download?directory=registration&fileName=" + car.getCarteGrisePath());
            return ResponseEntity.ok(carDTO);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @Operation(summary = "Approuver une voiture", description = "Approuver une voiture pour la rendre disponible en ligne et envoyer un email de confirmation.")
    @PostMapping("/approve/{id}")
    public ResponseEntity<CarDTO> approveCar(
            @Parameter(description = "L'ID de la voiture à approuver", required = true) @PathVariable Long id) {
        CarDTO approvedCar = carService.approveCar(id);
        if (approvedCar != null) {
            return ResponseEntity.ok(approvedCar);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @Operation(summary = "Rejeter une voiture", description = "Rejeter une voiture et envoyer un email de notification à l'utilisateur.")
    @PostMapping("/reject/{id}")
    public ResponseEntity<String> rejectCar(
            @Parameter(description = "L'ID de la voiture à rejeter", required = true) @PathVariable Long id) {
        boolean isRejected = carService.rejectCar(id);
        if (isRejected) {
            return ResponseEntity.ok("La voiture a été rejetée avec succès.");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Voiture non trouvée.");
        }
    }


    @GetMapping("/stats")
    public Map<String, Object> getKpiStats() {
        long totalCars = carService.countTotalCars();
        long onlineCars = carService.countOnlineCars();
        long rentedCars = carService.countCarsRented();
        long pendingCars = carService.countPendingCars();

        Map<String, Object> kpiData = new HashMap<>();
        kpiData.put("totalCars", totalCars);
        kpiData.put("percentageOnline", onlineCars * 100.0 / totalCars);
        kpiData.put("rentedCars", rentedCars);
        kpiData.put("percentagePending", pendingCars * 100.0 / totalCars);

        return kpiData;
    }

    @GetMapping("/reservations-by-month")
    public List<Map<String, Object>> getReservationsByMonth() {
        List<Object[]> results = carService.getReservationsByMonth();
        List<Map<String, Object>> reservationsData = new ArrayList<>();

        for (Object[] result : results) {
            Map<String, Object> data = new HashMap<>();
            data.put("month", result[0]);
            data.put("count", result[1]);
            reservationsData.add(data);
        }

        return reservationsData;
    }
}

