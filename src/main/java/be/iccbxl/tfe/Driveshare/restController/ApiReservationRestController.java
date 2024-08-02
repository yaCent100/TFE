package be.iccbxl.tfe.Driveshare.restController;

import be.iccbxl.tfe.Driveshare.DTO.IndisponibleDTO;
import be.iccbxl.tfe.Driveshare.DTO.ReservationDTO;
import be.iccbxl.tfe.Driveshare.service.serviceImpl.IndisponibleService;
import be.iccbxl.tfe.Driveshare.service.serviceImpl.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ApiReservationRestController {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private IndisponibleService indisponibleService;

    @GetMapping("/api/reservations")
    public List<ReservationDTO> getCurrentUserReservations() {
        return reservationService.getReservationsForCurrentUser();
    }

    @GetMapping("/api/unavailable-dates")
    public List<IndisponibleDTO> getUnavailableDates(@RequestParam Long carId) {
        List<IndisponibleDTO> dates = indisponibleService.findByCarId(carId);
        // Ajoutez un log pour vérifier les données
        System.out.println("Dates indisponibles pour la voiture ID " + carId + ": " + dates);
        return dates;
    }



}
