package be.iccbxl.tfe.Driveshare.restController.admin;

import be.iccbxl.tfe.Driveshare.DTO.EvaluationDTO;
import be.iccbxl.tfe.Driveshare.service.serviceImpl.EvaluationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminEvaluationRestController {
    @Autowired
    private EvaluationService evaluationService;

    @GetMapping("/evaluations")
    public List<EvaluationDTO> getAllEvaluationsGroupedByCar() {
        return evaluationService.getAllEvaluationsGroupedByCar();
    }

    @DeleteMapping("/evaluations/{id}")
    public ResponseEntity<Void> deleteEvaluation(@PathVariable Long id) {
        try {
            evaluationService.deleteEvaluationById(id);
            return ResponseEntity.noContent().build();  // Retourne un statut 204 No Content
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();  // Retourne un statut 404 Not Found si l'évaluation n'existe pas
        } catch (Exception e) {
            return ResponseEntity.status(500).build();  // Retourne un statut 500 Internal Server Error pour toute autre erreur
        }
    }
}
