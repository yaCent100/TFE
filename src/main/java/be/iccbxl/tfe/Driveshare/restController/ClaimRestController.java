package be.iccbxl.tfe.Driveshare.restController;

import be.iccbxl.tfe.Driveshare.DTO.ClaimDTO;
import be.iccbxl.tfe.Driveshare.DTO.MapperDTO;
import be.iccbxl.tfe.Driveshare.model.Claim;
import be.iccbxl.tfe.Driveshare.model.Reservation;
import be.iccbxl.tfe.Driveshare.model.User;
import be.iccbxl.tfe.Driveshare.service.serviceImpl.ClaimService;
import be.iccbxl.tfe.Driveshare.service.serviceImpl.ReservationService;
import be.iccbxl.tfe.Driveshare.service.serviceImpl.UserService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.*;

@RestController
@RequestMapping("/api/claims")
public class ClaimRestController {
    @Autowired
    private ClaimService claimService;
    @Autowired
    private ReservationService reservationService;

    @Autowired
    private UserService userService;

    @PostMapping("/submit")
    public ResponseEntity<ClaimDTO> submitClaim(@RequestBody Map<String, Object> payload) {
        String message = (String) payload.get("message");
        Long reservationId = Long.valueOf(String.valueOf(payload.get("reservationId")));
        String claimantRole = (String) payload.get("claimantRole");

        // Création de la plainte liée à la réservation et au rôle du plaignant
        Claim claim = claimService.createClaim(reservationId, message, claimantRole);

        // Utilisation du mapper pour convertir l'entité Claim en ClaimDto
        ClaimDTO claimDto = MapperDTO.toClaimDto(claim);

        return ResponseEntity.ok(claimDto);
    }



        // R2CLAMATION CLAIMROLE
        @GetMapping("/{reservationId}/role")
        public ResponseEntity<?> getUserRoleForReservation(@PathVariable Long reservationId) {
            // Obtenir l'utilisateur authentifié
            User authenticatedUser = userService.getAuthenticatedUser();

            if (authenticatedUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Utilisateur non authentifié.");
            }

            String username = authenticatedUser.getEmail(); // Obtenir l'email de l'utilisateur authentifié
            Reservation reservation = reservationService.getReservationById(reservationId);

            if (reservation == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Réservation introuvable.");
            }

            // Vérifier si l'utilisateur est le propriétaire de la voiture
            if (reservation.getCar().getUser().getEmail().equals(username)) {
                return ResponseEntity.ok(Collections.singletonMap("role", "PROPRIETAIRE"));
            }
            // Vérifier si l'utilisateur est le locataire de la voiture
            else if (reservation.getUser().getEmail().equals(username)) {
                return ResponseEntity.ok(Collections.singletonMap("role", "LOCATAIRE"));
            }

            // Si l'utilisateur n'est ni propriétaire ni locataire
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Accès refusé. Vous n'êtes ni le propriétaire ni le locataire pour cette réservation.");
        }

    @PostMapping("/userReply/{id}")
    public ResponseEntity<String> userReplyToClaim(@PathVariable Long id, @RequestBody Map<String, String> requestBody) {
        String userMessage = requestBody.get("message");

        // Récupérer la réclamation par son ID
        Claim claim = claimService.getById(id).orElseThrow();

        // Relancer la réclamation avec le nouveau message de l'utilisateur
        claim.reopenClaim(userMessage); // Cette méthode met à jour le statut et le message

        // Sauvegarder la réclamation mise à jour
        claimService.save(claim);

        return ResponseEntity.ok("Réclamation relancée");
    }

    @Operation(summary = "Filtrer les réclamations", description = "Retourne les réclamations en fonction des filtres appliqués.")
    @GetMapping("/filter")
    @ResponseBody
    public ResponseEntity<?> filterClaims(
            @RequestParam boolean enCours,
            @RequestParam boolean termine,
            @RequestParam boolean enAttente,
            Principal principal) {

        String email = principal.getName();
        User currentUser = userService.findByEmail(email);
        List<ClaimDTO> filteredClaims = new ArrayList<>();

        // Filtrer les réclamations par statut
        if (enCours) {
            filteredClaims.addAll(claimService.findClaimsByStatus(currentUser, "IN_PROGRESS"));
        }
        if (termine) {
            filteredClaims.addAll(claimService.findClaimsByStatus(currentUser, "FINISHED"));
        }
        if (enAttente) {
            filteredClaims.addAll(claimService.findClaimsByStatus(currentUser, "PENDING"));
        }

        if (filteredClaims.isEmpty()) {
            return ResponseEntity.ok("Aucune réclamation trouvée.");
        }

        String claimsHtml = claimService.renderClaimsHtml(filteredClaims, currentUser);


        return ResponseEntity.ok(claimsHtml); // Retourner la liste de ClaimDTO
    }



}
