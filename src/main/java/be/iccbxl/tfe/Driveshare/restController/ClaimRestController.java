package be.iccbxl.tfe.Driveshare.restController;

import be.iccbxl.tfe.Driveshare.DTO.ClaimDTO;
import be.iccbxl.tfe.Driveshare.DTO.MapperDTO;
import be.iccbxl.tfe.Driveshare.model.Car;
import be.iccbxl.tfe.Driveshare.model.Claim;
import be.iccbxl.tfe.Driveshare.model.Reservation;
import be.iccbxl.tfe.Driveshare.model.User;
import be.iccbxl.tfe.Driveshare.service.serviceImpl.ClaimService;
import be.iccbxl.tfe.Driveshare.service.serviceImpl.EmailService;
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

    @Autowired
    private EmailService emailService;

    @PostMapping("/submit")
    public ResponseEntity<?> submitClaim(@RequestBody Map<String, Object> payload) {
        String message = (String) payload.get("message");
        Long reservationId = Long.valueOf(String.valueOf(payload.get("reservationId")));
        String claimantRole = (String) payload.get("claimantRole");

        // Logs pour vérifier les données
        System.out.println("Message: " + message);
        System.out.println("Reservation ID: " + reservationId);
        System.out.println("Claimant Role: " + claimantRole);

        // Vérifier si une plainte existe déjà pour cette réservation
        if (claimService.existsByReservationId(reservationId)) {
            return ResponseEntity.badRequest().body("Une réclamation a déjà été soumise pour cette réservation.");
        }

        // Création de la réclamation liée à la réservation et au rôle du plaignant
        Claim claim = claimService.createClaim(reservationId, message, claimantRole);

        // Récupérer les informations de la réservation via la réclamation
        Reservation reservation = claim.getReservation();  // Accéder à la réservation liée à la réclamation

        if (reservation != null) {
            Car car = reservation.getCar();  // Accéder à la voiture liée à la réservation
            if (car != null) {
                // Récupérer les informations sur la voiture
                String carBrand = car.getBrand();
                String carModel = car.getModel();

                // Récupérer tous les administrateurs
                List<User> admins = userService.findByRole("ADMIN");
                if (!admins.isEmpty()) {
                    String[] adminEmails = admins.stream().map(User::getEmail).toArray(String[]::new);

                    // Inclure les détails de la réservation et de la voiture dans l'objet de l'e-mail
                    String subject = "Nouvelle réclamation pour la réservation n°" + reservationId + " (" + carBrand + " " + carModel + ")";
                    String body = "Une nouvelle réclamation a été soumise pour la voiture " + carBrand + " " + carModel + " :\n\n" + message;

                    // Envoyer l'e-mail de notification aux administrateurs
                    emailService.sendNotificationEmail(adminEmails, subject, body);
                }
            }
        }

        // Utilisation du mapper pour convertir l'entité Claim en ClaimDto
        ClaimDTO claimDto = MapperDTO.toClaimDto(claim);

        // Retourner la réponse avec la réclamation
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
    public ResponseEntity<Map<String, String>> userReplyToClaim(@PathVariable Long id, @RequestBody Map<String, String> requestBody) {
        String userMessage = requestBody.get("message");

        // Récupérer la réclamation par son ID
        Claim claim = claimService.getById(id).orElseThrow();

        // Relancer la réclamation avec le nouveau message de l'utilisateur
        claim.reopenClaim(userMessage); // Cette méthode met à jour le statut et le message

        // Sauvegarder la réclamation mise à jour
        claimService.save(claim);

        // Récupérer la réservation liée à la réclamation
        Reservation reservation = claim.getReservation();
        if (reservation != null) {
            // Récupérer les informations de la voiture liée à la réservation
            Car car = reservation.getCar();
            if (car != null) {
                String carBrand = car.getBrand();
                String carModel = car.getModel();
                Long reservationId = reservation.getId();

                // Récupérer tous les administrateurs
                List<User> admins = userService.findByRole("ADMIN");
                if (!admins.isEmpty()) {
                    String[] adminEmails = admins.stream().map(User::getEmail).toArray(String[]::new);

                    // Préparer l'objet et le corps de l'e-mail
                    String subject = "Nouvelle réponse à la réclamation pour la réservation n°" + reservationId + " (" + carBrand + " " + carModel + ")";
                    String body = "L'utilisateur a répondu à la réclamation pour la voiture " + carBrand + " " + carModel + " concernant la réservation n°" + reservationId + " :\n\n" + userMessage;

                    // Envoyer l'e-mail à tous les administrateurs
                    emailService.sendNotificationEmail(adminEmails, subject, body);
                }
            }
        }

        // Retourner une réponse JSON valide
        Map<String, String> response = new HashMap<>();
        response.put("message", "Réclamation relancée avec succès");

        return ResponseEntity.ok(response);  // Renvoie un objet JSON avec un message
    }



    @PostMapping("/close/{claimId}")
    public ResponseEntity<Map<String, String>> closeClaim(@PathVariable Long claimId) {
        // Rechercher la réclamation par son ID
        Claim claim = claimService.getById(claimId)
                .orElseThrow(() -> new RuntimeException("Réclamation non trouvée"));

        // Marquer la réclamation comme terminée
        claim.closeClaim();

        // Sauvegarder la réclamation dans la base de données
        claimService.save(claim);

        // Récupérer la réservation liée à la réclamation
        Reservation reservation = claim.getReservation();
        if (reservation != null) {
            // Récupérer les informations de la voiture liée à la réservation
            Car car = reservation.getCar();
            if (car != null) {
                String carBrand = car.getBrand();
                String carModel = car.getModel();
                Long reservationId = reservation.getId();

                // Récupérer tous les administrateurs
                List<User> admins = userService.findByRole("ADMIN");
                if (!admins.isEmpty()) {
                    String[] adminEmails = admins.stream().map(User::getEmail).toArray(String[]::new);

                    // Préparer l'objet et le corps de l'e-mail
                    String subject = "Réclamation clôturée pour la réservation n°" + reservationId + " (" + carBrand + " " + carModel + ")";
                    String body = "L'utilisateur a clôturé la réclamation pour la voiture " + carBrand + " " + carModel
                            + " concernant la réservation n°" + reservationId + ".\n\n"
                            + "Réclamation : " + claim.getMessage() + "\n\n"
                            + "Statut : " + claim.getStatus();

                    // Envoyer l'e-mail à tous les administrateurs
                    emailService.sendNotificationEmail(adminEmails, subject, body);
                }
            }
        }

        // Créer une réponse JSON
        Map<String, String> response = new HashMap<>();
        response.put("message", "Réclamation clôturée avec succès");

        // Retourner la réponse en JSON
        return ResponseEntity.ok(response);
    }



    @Operation(summary = "Filtrer les réclamations", description = "Retourne les réclamations en fonction des filtres appliqués.")
    @GetMapping("/filter")
    @ResponseBody
    public ResponseEntity<?> filterClaims(
            @RequestParam boolean enCours,
            @RequestParam boolean cloturer,
            @RequestParam boolean enAttente,
            Principal principal) {

        String email = principal.getName();
        User currentUser = userService.findByEmail(email);
        List<ClaimDTO> filteredClaims = new ArrayList<>();

        if (enCours) {
            filteredClaims.addAll(claimService.findClaimsByStatus(currentUser, "IN_PROGRESS"));
        }
        if (cloturer) {
            filteredClaims.addAll(claimService.findClaimsByStatus(currentUser, "FINISHED"));
        }
        if (enAttente) {
            filteredClaims.addAll(claimService.findClaimsByStatus(currentUser, "PENDING"));
        }

        if (filteredClaims.isEmpty()) {
            return ResponseEntity.ok("Aucune réclamation trouvée.");
        }

        String claimsHtml = claimService.renderClaimsHtml(filteredClaims, currentUser);
        return ResponseEntity.ok(claimsHtml);
    }




}
