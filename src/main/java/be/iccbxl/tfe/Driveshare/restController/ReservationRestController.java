package be.iccbxl.tfe.Driveshare.restController;

import be.iccbxl.tfe.Driveshare.DTO.CarMapper;
import be.iccbxl.tfe.Driveshare.DTO.ReservationDTO;

import be.iccbxl.tfe.Driveshare.model.Reservation;
import be.iccbxl.tfe.Driveshare.model.User;
import be.iccbxl.tfe.Driveshare.security.CustomUserDetail;
import be.iccbxl.tfe.Driveshare.service.serviceImpl.DateService;
import be.iccbxl.tfe.Driveshare.service.serviceImpl.EmailService;
import be.iccbxl.tfe.Driveshare.service.serviceImpl.ReservationService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class ReservationRestController {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private EmailService emailService;
    @Autowired
    private DateService dateService;

    private static final Logger logger = LoggerFactory.getLogger(ReservationRestController.class);

    @GetMapping("/api/getOwnerReservations")
    public List<ReservationDTO> getOwnerReservations(@RequestParam List<Long> carIds) {
        List<Reservation> reservations = reservationService.getReservationsByCarIds(carIds);
        return reservations.stream().map(CarMapper::toReservationDTO).collect(Collectors.toList());
    }

    @GetMapping("/api/getRenterReservations")
    public ResponseEntity<List<ReservationDTO>> getRenterReservations(@RequestParam List<String> statuses, @AuthenticationPrincipal CustomUserDetail userDetails) {
        Logger logger = LoggerFactory.getLogger(ReservationRestController.class);

        if (userDetails == null) {
            logger.warn("User details are null, unauthorized access attempt.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User user = userDetails.getUser();
        logger.info("Fetching reservations for user: {}", user.getNom());
        logger.info("Requested statuses: {}", statuses);

        List<Reservation> reservations = reservationService.getReservationsByStatusesAndUser(statuses, user);

        if (reservations.isEmpty()) {
            logger.info("No reservations found for the given statuses and user.");
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }

        List<ReservationDTO> reservationDTOs = reservations.stream()
                .map(CarMapper::toReservationDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(reservationDTOs);
    }

    @GetMapping("/api/getReservationDetails")
    public ReservationDTO getReservationDetails(@RequestParam Long reservationId) {
        Reservation reservation = reservationService.getReservationById(reservationId);
        if (reservation == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Reservation not found");
        }
        return CarMapper.toReservationDTO(reservation);
    }

    @GetMapping("/api/format-date")
    public ResponseEntity<Map<String, String>> formatDates(@RequestParam("debut") String debut, @RequestParam("fin") String fin) {
        LocalDate debutDate = LocalDate.parse(debut);
        LocalDate finDate = LocalDate.parse(fin);
        String formattedDebutDate = dateService.formatAndCapitalizeDate(debutDate);
        String formattedFinDate = dateService.formatAndCapitalizeDate(finDate);

        Map<String, String> response = new HashMap<>();
        response.put("formattedDebutDate", formattedDebutDate);
        response.put("formattedFinDate", formattedFinDate);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/api/cancelReservation/{id}")
    public ResponseEntity<Map<String, Object>> cancelReservation(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetail userDetails) {
        Map<String, Object> response = new HashMap<>();

        Reservation reservation = reservationService.getReservationById(id);
        if (reservation == null) {
            response.put("success", false);
            response.put("message", "Réservation non trouvée.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        User currentUser = userDetails.getUser();

        // Vérifiez si l'utilisateur est autorisé à annuler cette réservation
        if (!reservation.getUser().equals(currentUser) && !reservation.getCar().getUser().equals(currentUser)) {
            response.put("success", false);
            response.put("message", "Vous n'êtes pas autorisé à annuler cette réservation.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }

        reservationService.cancelReservation(id);
        response.put("success", true);
        response.put("message", "La réservation a été annulée avec succès.");
        return ResponseEntity.ok(response);
    }


    @PostMapping("/reservation/{id}/accept")
    public ResponseEntity<Map<String, String>> acceptReservation(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetail userDetails) {
        Map<String, String> response = new HashMap<>();
        Reservation reservation = reservationService.getReservationById(id);
        if (reservation == null) {
            response.put("status", "error");
            response.put("message", "Reservation not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        User user = userDetails.getUser();
        if (!reservation.getCar().getUser().getId().equals(user.getId())) {
            response.put("status", "error");
            response.put("message", "You are not allowed to accept this reservation");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }

        reservation.setStatut("CONFIRMED");
        reservationService.saveReservation(reservation);

        // Envoyer une notification à l'utilisateur
        String userEmail = reservation.getUser().getEmail();
        String subject = "Votre réservation a été acceptée";
        String text = "Votre réservation pour la voiture " + reservation.getCar().getBrand() + " " + reservation.getCar().getModel() +
                " du " + reservation.getDebutLocation() + " au " + reservation.getFinLocation() + " a été acceptée.";
        emailService.sendNotificationEmail(userEmail, subject, text);

        response.put("status", "ok");
        response.put("message", "Reservation accepted");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reservation/{id}/reject")
    public ResponseEntity<Map<String, String>> rejectReservation(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetail userDetails) {
        Map<String, String> response = new HashMap<>();
        Reservation reservation = reservationService.getReservationById(id);
        if (reservation == null) {
            response.put("status", "error");
            response.put("message", "Reservation not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        User user = userDetails.getUser();
        if (!reservation.getCar().getUser().getId().equals(user.getId())) {
            response.put("status", "error");
            response.put("message", "You are not allowed to reject this reservation");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }

        reservation.setStatut("REJECTED");
        reservationService.saveReservation(reservation);

        // Envoyer une notification à l'utilisateur
        String userEmail = reservation.getUser().getEmail();
        String subject = "Votre réservation a été refusée";
        String text = "Votre réservation pour la voiture " + reservation.getCar().getBrand() + " " + reservation.getCar().getModel() +
                " du " + reservation.getDebutLocation() + " au " + reservation.getFinLocation() + " a été refusée.";
        emailService.sendNotificationEmail(userEmail, subject, text);

        response.put("status", "ok");
        response.put("message", "Reservation rejected");
        return ResponseEntity.ok(response);
    }





}
