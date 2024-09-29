package be.iccbxl.tfe.Driveshare.restController;

import be.iccbxl.tfe.Driveshare.DTO.MapperDTO;
import be.iccbxl.tfe.Driveshare.DTO.ReservationDTO;

import be.iccbxl.tfe.Driveshare.model.*;
import be.iccbxl.tfe.Driveshare.security.CustomUserDetail;
import be.iccbxl.tfe.Driveshare.service.serviceImpl.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@Tag(name = "Reservation Management", description = "Gestion des réservations et des annulations")
public class ReservationRestController {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private DateService dateService;

    @Autowired
    private RefundService refundService;

    @Autowired
    private CarService carService;


    private static final Logger logger = LoggerFactory.getLogger(ReservationRestController.class);

    @Operation(summary = "Obtenir les réservations du propriétaire", description = "Retourne toutes les réservations pour les voitures d'un propriétaire.")
    @GetMapping("/api/getOwnerReservations")
    public ResponseEntity<List<ReservationDTO>> getOwnerReservations(
            @Parameter(description = "Liste des IDs des voitures du propriétaire", required = true) @RequestParam List<Long> carIds) {

        // Vérifier que la liste des IDs n'est pas vide
        if (carIds == null || carIds.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();  // Renvoie 204 si la liste est vide
        }

        List<Reservation> reservations = reservationService.getReservationsByCarIds(carIds);

        // Renvoie 204 si aucune réservation trouvée
        if (reservations.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }

        List<ReservationDTO> reservationDTOs = reservations.stream()
                .map(MapperDTO::toReservationDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(reservationDTOs);
    }



    @Operation(summary = "Obtenir les réservations du locataire", description = "Retourne les réservations en fonction de l'état pour le locataire connecté.")
    @GetMapping("/api/getRenterReservations")
    public ResponseEntity<List<ReservationDTO>> getRenterReservations(
            @Parameter(description = "Liste des statuts des réservations", required = true) @RequestParam List<String> statuses,
            @AuthenticationPrincipal CustomUserDetail userDetails) {

        // Vérifier que la liste des statuts n'est pas vide
        if (statuses == null || statuses.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();  // Renvoie 204 si aucun statut n'est fourni
        }

        User user = userDetails.getUser();
        List<Reservation> reservations = reservationService.getReservationsByStatusesAndUser(statuses, user);

        // Renvoie 204 si aucune réservation trouvée pour les statuts donnés
        if (reservations.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }

        List<ReservationDTO> reservationDTOs = reservations.stream()
                .map(MapperDTO::toReservationDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(reservationDTOs);
    }



    @Operation(summary = "Annuler une réservation", description = "Permet d'annuler une réservation et de calculer les remboursements.")
    @PostMapping("/api/cancelReservation/{id}")
    public ResponseEntity<Map<String, Object>> cancelReservation(
            @Parameter(description = "ID de la réservation à annuler", required = true) @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetail userDetails) {
        Map<String, Object> response = new HashMap<>();
        Reservation reservation = reservationService.getReservationById(id);
        if (reservation == null) {
            response.put("success", false);
            response.put("message", "Réservation non trouvée.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        User currentUser = userDetails.getUser();
        if (!reservation.getUser().equals(currentUser) && !reservation.getCar().getUser().equals(currentUser)) {
            response.put("success", false);
            response.put("message", "Vous n'êtes pas autorisé à annuler cette réservation.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startDate = reservation.getStartLocation().atStartOfDay();
        long hoursBetween = Duration.between(now, startDate).toHours();

        double refundPercentage = 0;
        if (hoursBetween >= 48) {
            refundPercentage = 0.90;
        } else if (hoursBetween >= 24) {
            refundPercentage = 0.50;
        }

        Payment payment = reservation.getPayment();
        if (payment != null) {
            double refundAmount = payment.getPrixTotal() * refundPercentage;
            Refund refund = new Refund();
            refund.setAmount(refundAmount);
            refund.setRefundDate(now);
            refund.setRefundPercentage(refundPercentage * 100);
            refund.setPayment(payment);
            refundService.saveRefund(refund);

            payment.setRefund(refund);
            paymentService.save(payment);

            response.put("refundAmount", refundAmount);
        }

        reservation.setStatut("CANCELLED");
        reservationService.saveReservation(reservation);

        response.put("success", true);
        response.put("message", "La réservation a été annulée avec succès.");
        return ResponseEntity.ok(response);
    }


    @GetMapping("/api/format-date")
    public Map<String, String> formatDate(
            @RequestParam("debut") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate debut,
            @RequestParam("fin") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE d MMM yyyy", java.util.Locale.FRENCH);

        String formattedDebutDate = debut.format(formatter);
        String formattedFinDate = fin.format(formatter);

        Map<String, String> formattedDates = new HashMap<>();
        formattedDates.put("formattedDebutDate", formattedDebutDate);
        formattedDates.put("formattedFinDate", formattedFinDate);

        return formattedDates;
    }


    @GetMapping("/api/check-availability")
    @ResponseBody
    public Map<String, Object> checkAvailability(@RequestParam("carId") Long carId,
                                                 @RequestParam("dateDebut") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dateDebut,
                                                 @RequestParam("dateFin") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dateFin) {
        Map<String, Object> response = new HashMap<>();

        Car car = carService.getCarById(carId); // Récupérer la voiture en question
        boolean available = carService.isCarAvailableForDates(car, dateDebut, dateFin);
        response.put("available", available);

        return response;
    }



    @PostMapping("/reservation/{reservationId}/action")
    public ResponseEntity<?> handleReservationAction(
            @PathVariable Long reservationId,
            @RequestParam String action) {
        try {
            Reservation reservation = reservationService.getReservationById(reservationId);
            if (reservation == null) {
                return ResponseEntity.status(404).body(Map.of("status", "error", "message", "Réservation introuvable"));
            }

            if ("accept".equalsIgnoreCase(action)) {
                reservation.setStatut("PAYMENT_PENDING");
                reservation.setCreatedAt(LocalDateTime.now()); // Enregistrer l'heure à laquelle le paiement a été demandé
                reservationService.saveReservation(reservation);
                return ResponseEntity.ok(Map.of("status", "ok", "message", "Réservation acceptée. En attente de paiement par le locataire."));
            } else if ("reject".equalsIgnoreCase(action)) {
                reservation.setStatut("REJECTED");
                reservationService.saveReservation(reservation);
                return ResponseEntity.ok(Map.of("status", "ok", "message", "Réservation refusée."));
            } else {
                return ResponseEntity.status(400).body(Map.of("status", "error", "message", "Action non valide."));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("status", "error", "message", "Erreur lors de l'action sur la réservation."));
        }
    }

    @PostMapping("/api/reservation/{reservationId}/expire")
    public ResponseEntity<?> expireReservation(@PathVariable Long reservationId) {
        Reservation reservation = reservationService.getReservationById(reservationId);
        if (reservation == null) {
            return ResponseEntity.status(404).body("Réservation introuvable");
        }

        if ("PAYMENT_PENDING".equals(reservation.getStatut())) {
            reservation.setStatut("EXPIRED");
            reservationService.saveReservation(reservation);
            return ResponseEntity.ok("Réservation expirée en raison d'un paiement manquant.");
        }

        return ResponseEntity.status(400).body("La réservation n'est pas en attente de paiement.");
    }

}
