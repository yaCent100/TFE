package be.iccbxl.tfe.Driveshare.restController;

import be.iccbxl.tfe.Driveshare.DTO.EvaluationDTO;
import be.iccbxl.tfe.Driveshare.service.serviceImpl.EvaluationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Review Management", description = "Gestion des évaluations de voitures")
public class EvaluationRestController {

    @Autowired
    private EvaluationService evaluationService;

    @Operation(summary = "Créer une évaluation", description = "Permet à un utilisateur de créer une évaluation pour une voiture.")
    @PostMapping("/api/review")
    public ResponseEntity<EvaluationDTO> createEvaluation(
            @Parameter(description = "Les détails de l'évaluation", required = true) @RequestBody EvaluationDTO evaluationDTO) {
        try {
            EvaluationDTO createdEvaluation = evaluationService.createEvaluation(evaluationDTO);
            return ResponseEntity.ok(createdEvaluation);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @Operation(summary = "Vérifier si une évaluation existe", description = "Vérifie si une évaluation existe déjà pour une réservation donnée.")
    @GetMapping("/evaluations/exists/{reservationId}")
    public ResponseEntity<Boolean> evaluationExists(@PathVariable Long reservationId) {
        boolean exists = evaluationService.evaluationExists(reservationId);
        return ResponseEntity.ok(exists);
    }
}

