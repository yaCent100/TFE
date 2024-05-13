package be.iccbxl.tfe.Driveshare.controller;

import be.iccbxl.tfe.Driveshare.DTO.ReservationDTO;
import be.iccbxl.tfe.Driveshare.model.Reservation;
import be.iccbxl.tfe.Driveshare.service.serviceImpl.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class CalendrierRestController {

    @Autowired
    private ReservationService reservationService;

    @GetMapping("/api/reservations")
    public List<ReservationDTO> getCurrentUserReservations() {
        return reservationService.getReservationsForCurrentUser();
    }
}
