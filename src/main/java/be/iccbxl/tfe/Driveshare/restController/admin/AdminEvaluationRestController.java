package be.iccbxl.tfe.Driveshare.restController.admin;

import be.iccbxl.tfe.Driveshare.DTO.EvaluationDTO;
import be.iccbxl.tfe.Driveshare.DTO.EvaluationDashboardDTO;
import be.iccbxl.tfe.Driveshare.service.serviceImpl.EvaluationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "Admin Evaluation Management", description = "API pour la gestion des évaluations par les administrateurs")
public class AdminEvaluationRestController {

    @Autowired
    private EvaluationService evaluationService;

    @Operation(summary = "Obtenir toutes les évaluations groupées par voiture", description = "Récupérer toutes les évaluations disponibles, groupées par voiture.")
    @GetMapping("/evaluations")
    public List<EvaluationDTO> getAllEvaluationsGroupedByCar() {
        return evaluationService.getAllEvaluationsGroupedByCar();
    }

    @Operation(summary = "Supprimer une évaluation", description = "Supprimer une évaluation spécifique par son ID.")
    @DeleteMapping("/evaluations/{id}")
    public ResponseEntity<Void> deleteEvaluation(
            @Parameter(description = "L'ID de l'évaluation à supprimer", required = true) @PathVariable Long id) {
        try {
            evaluationService.deleteEvaluationById(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Obtenir les données du tableau de bord des évaluations", description = "Récupérer les KPI et les données pour le graphique.")
    @GetMapping("/evaluations/dashboard")
    public ResponseEntity<EvaluationDashboardDTO> getEvaluationDashboardData() {
        EvaluationDashboardDTO dashboardData = evaluationService.getEvaluationDashboardData();
        return ResponseEntity.ok(dashboardData);
    }

    @Operation(summary = "Obtenir les évaluations pour une voiture", description = "Récupérer les évaluations spécifiques pour une voiture donnée par son ID.")
    @GetMapping("/evaluations/car/{carId}")
    public ResponseEntity<List<EvaluationDTO>> getEvaluationsByCarId(@PathVariable Long carId) {
        try {
            List<EvaluationDTO> evaluations = evaluationService.getEvaluationsByCarId(carId);
            return ResponseEntity.ok(evaluations);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
