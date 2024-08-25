package be.iccbxl.tfe.Driveshare.restController.admin;


import be.iccbxl.tfe.Driveshare.DTO.ClaimDTO;
import be.iccbxl.tfe.Driveshare.DTO.MapperDTO;
import be.iccbxl.tfe.Driveshare.model.Claim;
import be.iccbxl.tfe.Driveshare.model.User;
import be.iccbxl.tfe.Driveshare.security.CustomUserDetail;
import be.iccbxl.tfe.Driveshare.service.serviceImpl.ClaimService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/claims")
public class AdminClaimRestController {

    @Autowired
    private ClaimService claimService;

    // Récupérer toutes les réclamations
    // Obtenir toutes les réclamations
    @GetMapping
    public ResponseEntity<List<ClaimDTO>> getAllClaims() {
        List<Claim> claims = claimService.getALlClaims();
        List<ClaimDTO> claimDTOs = claims.stream().map(MapperDTO::toClaimDto).collect(Collectors.toList());
        return ResponseEntity.ok(claimDTOs);
    }

    // Résoudre une réclamation
    @PostMapping("/resolve/{id}")
    public ResponseEntity<String> resolveClaim(@PathVariable Long id) {
        claimService.resolveClaim(id);
        return ResponseEntity.ok("Réclamation résolue");
    }

    // Endpoint pour répondre à une réclamation
    @PostMapping("/response/{id}")
    public ResponseEntity<String> addResponseToClaim(@PathVariable Long id, @RequestBody Map<String, String> requestBody) {
        String message = requestBody.get("message");
        claimService.addResponseToClaim(id, message);
        return ResponseEntity.ok("Réponse envoyée");
    }

    @GetMapping("/reservation/{reservationId}")
    public ResponseEntity<List<ClaimDTO>> getClaimsByReservation(@PathVariable Long reservationId) {
        List<Claim> claims = claimService.getClaimsByReservation(reservationId);
        List<ClaimDTO> claimDTOs = claims.stream().map(MapperDTO::toClaimDto).collect(Collectors.toList());
        return ResponseEntity.ok(claimDTOs);
    }

    @GetMapping("/kpi")
    public ResponseEntity<Map<String, Long>> getClaimsKpi() {
        Map<String, Long> kpi = new HashMap<>();
        kpi.put("totalClaims", claimService.countAllClaims());
        kpi.put("resolvedClaims", claimService.countResolvedClaims());
        kpi.put("pendingClaims", claimService.countPendingClaims());
        return ResponseEntity.ok(kpi);
    }

    @GetMapping("/monthly")
    public ResponseEntity<Map<String, Long>> getMonthlyClaims() {
        Map<String, Long> claimsPerMonth = new LinkedHashMap<>();  // Utilisez LinkedHashMap pour préserver l'ordre des mois

        // Initialiser tous les mois avec 0 réclamations (les 12 derniers mois)
        for (int i = 11; i >= 0; i--) {
            String monthYear = LocalDateTime.now().minusMonths(i).format(DateTimeFormatter.ofPattern("MM-yyyy"));
            claimsPerMonth.put(monthYear, 0L);
        }

        // Définir la date de début comme étant 12 mois en arrière
        LocalDateTime startDate = LocalDateTime.now().minusMonths(12);

        // Récupérer les réclamations par mois pour les 12 derniers mois
        List<Object[]> results = claimService.countClaimsByMonthForLastYear(startDate);

        // Remplir la Map avec les valeurs réelles retournées par la base de données
        for (Object[] result : results) {
            String monthYear = (String) result[0];  // La chaîne formatée "MM-YYYY"
            Long count = (Long) result[1];           // Le nombre de réclamations
            claimsPerMonth.put(monthYear, count);    // Mettre à jour avec les valeurs réelles
        }

        return ResponseEntity.ok(claimsPerMonth);
    }


    @PostMapping("/close/{id}")
    public ResponseEntity<String> closeClaim(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetail userDetails) {
        // Récupérer l'utilisateur connecté
        User currentUser = userDetails.getUser();

        // Vérifier si l'utilisateur a le rôle d'administrateur
        if (!currentUser.getRoles().stream().anyMatch(role -> role.getRole().equals("Admin"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Vous n'avez pas l'autorisation de clore cette réclamation.");
        }

        // Récupérer la réclamation par son ID
       Claim claim = claimService.getById(id).orElseThrow();

        // Clôturer la réclamation
        claim.setStatus("TERMINE");
        claimService.save(claim);

        return ResponseEntity.ok("Réclamation clôturée par l'administrateur.");
    }





}
