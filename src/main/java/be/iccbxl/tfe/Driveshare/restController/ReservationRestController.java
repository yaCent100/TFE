package be.iccbxl.tfe.Driveshare.restController;

import be.iccbxl.tfe.Driveshare.DTO.MapperDTO;
import be.iccbxl.tfe.Driveshare.DTO.ReservationDTO;

import be.iccbxl.tfe.Driveshare.model.Payment;
import be.iccbxl.tfe.Driveshare.model.Refund;
import be.iccbxl.tfe.Driveshare.model.Reservation;
import be.iccbxl.tfe.Driveshare.model.User;
import be.iccbxl.tfe.Driveshare.security.CustomUserDetail;
import be.iccbxl.tfe.Driveshare.service.serviceImpl.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
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

    private static final Logger logger = LoggerFactory.getLogger(ReservationRestController.class);

    @Operation(summary = "Obtenir les réservations du propriétaire", description = "Retourne toutes les réservations pour les voitures d'un propriétaire.")
    @GetMapping("/api/getOwnerReservations")
    public List<ReservationDTO> getOwnerReservations(
            @Parameter(description = "Liste des IDs des voitures du propriétaire", required = true) @RequestParam List<Long> carIds) {
        List<Reservation> reservations = reservationService.getReservationsByCarIds(carIds);
        return reservations.stream().map(MapperDTO::toReservationDTO).collect(Collectors.toList());
    }

    @Operation(summary = "Obtenir les réservations du locataire", description = "Retourne les réservations en fonction de l'état pour le locataire connecté.")
    @GetMapping("/api/getRenterReservations")
    public ResponseEntity<List<ReservationDTO>> getRenterReservations(
            @Parameter(description = "Liste des statuts des réservations", required = true) @RequestParam List<String> statuses,
            @AuthenticationPrincipal CustomUserDetail userDetails) {
        User user = userDetails.getUser();
        List<Reservation> reservations = reservationService.getReservationsByStatusesAndUser(statuses, user);
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
}
