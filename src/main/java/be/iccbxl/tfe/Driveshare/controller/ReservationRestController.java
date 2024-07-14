package be.iccbxl.tfe.Driveshare.controller;

import be.iccbxl.tfe.Driveshare.DTO.CarMapper;
import be.iccbxl.tfe.Driveshare.DTO.ReservationDTO;
import be.iccbxl.tfe.Driveshare.model.CarRental;
import be.iccbxl.tfe.Driveshare.model.Reservation;
import be.iccbxl.tfe.Driveshare.model.User;
import be.iccbxl.tfe.Driveshare.security.CustomUserDetail;
import be.iccbxl.tfe.Driveshare.service.serviceImpl.CarRentalService;
import be.iccbxl.tfe.Driveshare.service.serviceImpl.ReservationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class ReservationRestController {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private CarRentalService carRentalService;

    private static final Logger logger = LoggerFactory.getLogger(ReservationRestController.class);
    @GetMapping("/api/getOwnerReservations")
    public List<ReservationDTO> getOwnerReservations(@RequestParam List<Long> carIds) {
        List<Reservation> reservations = reservationService.getReservationsByCarIds(carIds);
        return reservations.stream().map(CarMapper::toReservationDTO).collect(Collectors.toList());
    }

    @GetMapping("/api/getRenterReservations")
    public List<ReservationDTO> getRenterReservations(@RequestParam List<String> statuses, @AuthenticationPrincipal CustomUserDetail userDetails) {
        User user = userDetails.getUser();
        List<Reservation> reservations = reservationService.getReservationsByStatusesAndUser(statuses, user);
        return reservations.stream().map(CarMapper::toReservationDTO).collect(Collectors.toList());
    }

    @GetMapping("/api/getReservationDetails")
    public ReservationDTO getReservationDetails(@RequestParam Long reservationId) {
        Reservation reservation = reservationService.getReservationById(reservationId);
        if (reservation == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Reservation not found");
        }
        return CarMapper.toReservationDTO(reservation);
    }

}
