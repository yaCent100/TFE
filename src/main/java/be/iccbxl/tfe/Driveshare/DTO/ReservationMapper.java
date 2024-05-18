package be.iccbxl.tfe.Driveshare.DTO;

import be.iccbxl.tfe.Driveshare.model.Reservation;

public class ReservationMapper {

    // Méthode de transformation Entité -> DTO
    public static ReservationDTO toDto(Reservation reservation) {
        ReservationDTO dto = new ReservationDTO();
        dto.setId(reservation.getId());
        dto.setDebutLocation(reservation.getDebutLocation());
        dto.setFinLocation(reservation.getFinLocation());
        dto.setCreatedAt(reservation.getCreatedAt());
        dto.setCarName(reservation.getCarRental().getCar().getModel());
        dto.setUserName(reservation.getCarRental().getUser().getNom());
        dto.setStatut(reservation.getStatut());
        dto.setNbJours(reservation.getNbJours());
        return dto;
    }
}
