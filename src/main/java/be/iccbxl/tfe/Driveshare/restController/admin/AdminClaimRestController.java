package be.iccbxl.tfe.Driveshare.restController.admin;


import be.iccbxl.tfe.Driveshare.DTO.ClaimDTO;
import be.iccbxl.tfe.Driveshare.DTO.MapperDTO;
import be.iccbxl.tfe.Driveshare.model.Car;
import be.iccbxl.tfe.Driveshare.model.Claim;
import be.iccbxl.tfe.Driveshare.model.Reservation;
import be.iccbxl.tfe.Driveshare.model.User;
import be.iccbxl.tfe.Driveshare.security.CustomUserDetail;
import be.iccbxl.tfe.Driveshare.service.serviceImpl.ClaimService;
import be.iccbxl.tfe.Driveshare.service.serviceImpl.EmailService;
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

    @Autowired
    private EmailService emailService;

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
        // Récupérer le message de la réponse de l'administrateur
        String adminMessage = requestBody.get("message");

        // Ajouter la réponse à la réclamation
        claimService.addResponseToClaim(id, adminMessage);

        // Récupérer la réclamation par son ID
        Claim claim = claimService.getById(id)
                .orElseThrow(() -> new RuntimeException("Réclamation non trouvée"));

        // Récupérer la réservation liée à la réclamation
        Reservation reservation = claim.getReservation();
        if (reservation != null) {
            // Récupérer les informations sur la voiture et l'utilisateur
            Car car = reservation.getCar();
            User user = reservation.getUser(); // L'utilisateur qui a fait la réservation

            if (car != null && user != null) {
                String carBrand = car.getBrand();
                String carModel = car.getModel();
                Long reservationId = reservation.getId();
                String userEmail = user.getEmail(); // E-mail de l'utilisateur

                // Préparer l'objet et le corps de l'e-mail
                String subject = "Réponse à votre réclamation pour la réservation n°" + reservationId + " (" + carBrand + " " + carModel + ")";
                String body = "Bonjour " + user.getFirstName() + ",\n\n"
                        + "L'administrateur a répondu à votre réclamation concernant la voiture " + carBrand + " " + carModel
                        + " pour la réservation n°" + reservationId + ".\n\n"
                        + "Réclamation initiale : " + claim.getMessage() + "\n\n"
                        + "Réponse de l'administrateur : " + adminMessage + "\n\n"
                        + "Cordialement,\nL'équipe administrative";

                // Envoyer l'e-mail à l'utilisateur
                emailService.sendNotificationEmail(new String[]{userEmail}, subject, body);
            }
        }

        // Retourner une réponse JSON
        return ResponseEntity.ok("Réponse envoyée et notification e-mail envoyée à l'utilisateur.");
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
        long totalClaims = claimService.countAllClaims();
        long resolvedClaims = claimService.countResolvedClaims();
        long inProgressClaims = claimService.countInProgressClaims(); // Ajout pour réclamations en cours
        long pendingClaims = claimService.countPendingClaims();

        kpi.put("totalClaims", totalClaims);
        kpi.put("resolvedClaims", resolvedClaims);
        kpi.put("inProgressClaims", inProgressClaims);
        kpi.put("pendingClaims", pendingClaims);

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


    @PostMapping("/admin/close/{id}")
    public ResponseEntity<String> closeClaim(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetail userDetails) {
        // Récupérer l'utilisateur connecté
        User currentUser = userDetails.getUser();

        // Vérifier si l'utilisateur a le rôle d'administrateur
        if (!currentUser.getRoles().stream().anyMatch(role -> role.getRole().equals("Admin"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Vous n'avez pas l'autorisation de clore cette réclamation.");
        }

        // Récupérer la réclamation par son ID
        Claim claim = claimService.getById(id)
                .orElseThrow(() -> new RuntimeException("Réclamation non trouvée"));

        // Clôturer la réclamation
        claim.setStatus("FINISHED");
        claimService.save(claim);

        // Récupérer les détails de la réservation liée
        Reservation reservation = claim.getReservation();
        if (reservation != null) {
            // Récupérer les informations sur la voiture
            Car car = reservation.getCar();
            String carBrand = car.getBrand();
            String carModel = car.getModel();
            Long reservationId = reservation.getId();

            // Vérifier le rôle du plaignant (propriétaire ou locataire)
            String claimantRole = claim.getClaimantRole();
            User userToNotify;

            if ("PROPRIETAIRE".equalsIgnoreCase(claimantRole)) {
                // Si le plaignant est le propriétaire, notifier le locataire
                userToNotify = reservation.getUser();
            } else if ("LOCATAIRE".equalsIgnoreCase(claimantRole)) {
                // Si le plaignant est le locataire, notifier le propriétaire
                userToNotify = car.getUser();
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Rôle de plaignant invalide.");
            }

            if (userToNotify != null) {
                String userEmail = userToNotify.getEmail();
                String userName = userToNotify.getFirstName();

                // Préparer l'objet et le corps de l'e-mail
                String subject = "Réclamation clôturée pour la réservation n°" + reservationId + " (" + carBrand + " " + carModel + ")";
                String body = "Bonjour " + userName + ",\n\n"
                        + "La réclamation concernant la voiture " + carBrand + " " + carModel
                        + " pour la réservation n°" + reservationId + " a été clôturée par l'administrateur.\n\n"
                        + "Réclamation initiale : " + claim.getMessage() + "\n\n"
                        + "Merci pour votre compréhension.\n"
                        + "Cordialement,\nL'équipe administrative";

                // Envoyer l'e-mail
                emailService.sendNotificationEmail(new String[]{userEmail}, subject, body);
            }
        }

        return ResponseEntity.ok("Réclamation clôturée par l'administrateur et notification e-mail envoyée.");
    }







}
