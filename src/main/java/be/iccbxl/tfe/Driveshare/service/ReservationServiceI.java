package be.iccbxl.tfe.Driveshare.service;

import be.iccbxl.tfe.Driveshare.DTO.ReservationDTO;
import be.iccbxl.tfe.Driveshare.model.Car;
import be.iccbxl.tfe.Driveshare.model.Reservation;
import be.iccbxl.tfe.Driveshare.model.User;

import java.util.List;

public interface ReservationServiceI {

    List<Reservation> getAllReservations();
    Reservation getReservationById(Long id);
    Reservation addReservation(Reservation reservation);

    Reservation saveReservation(Reservation reservation);

    Reservation updateReservation(Long id, Reservation reservation);
    void deleteReservation(Long id);

    List<ReservationDTO> getReservationsForCurrentUser();



    List<Reservation> getReservationsByStatusesAndUser(List<String> statuses, User user);
    List<Reservation> getReservationsByCarIds(List<Long> carIds);
}
