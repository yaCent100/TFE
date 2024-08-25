package be.iccbxl.tfe.Driveshare.restController;

import be.iccbxl.tfe.Driveshare.DTO.IndisponibleDTO;
import be.iccbxl.tfe.Driveshare.DTO.ReservationDTO;
import be.iccbxl.tfe.Driveshare.service.serviceImpl.IndisponibleService;
import be.iccbxl.tfe.Driveshare.service.serviceImpl.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Tag(name = "Reservation Management", description = "Gestion des réservations")
public class ApiReservationRestController {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private IndisponibleService indisponibleService;

    @Operation(summary = "Obtenir les réservations de l'utilisateur courant", description = "Retourne la liste des réservations pour l'utilisateur connecté.")
    @GetMapping("/api/reservations")
    public List<ReservationDTO> getCurrentUserReservations() {
        return reservationService.getReservationsForCurrentUser();
    }

    @Operation(summary = "Obtenir les dates d'indisponibilité d'une voiture", description = "Retourne les dates d'indisponibilité d'une voiture spécifique.")
    @GetMapping("/api/unavailable-dates")
    public List<IndisponibleDTO> getUnavailableDates(
            @Parameter(description = "L'ID de la voiture pour laquelle obtenir les dates", required = true) @RequestParam Long carId) {
        return indisponibleService.findByCarId(carId);
    }
}

