package be.iccbxl.tfe.Driveshare.service.serviceImpl;

import be.iccbxl.tfe.Driveshare.DTO.ReservationDTO;
import be.iccbxl.tfe.Driveshare.DTO.ReservationMapper;
import be.iccbxl.tfe.Driveshare.model.Car;
import be.iccbxl.tfe.Driveshare.model.CarRental;
import be.iccbxl.tfe.Driveshare.model.Reservation;
import be.iccbxl.tfe.Driveshare.model.User;
import be.iccbxl.tfe.Driveshare.repository.CarRentalRepository;
import be.iccbxl.tfe.Driveshare.repository.ReservationRepository;
import be.iccbxl.tfe.Driveshare.repository.UserRepository;
import be.iccbxl.tfe.Driveshare.service.ReservationServiceI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReservationService implements ReservationServiceI {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CarRentalRepository carRentalRepository;


    @Override
    public List<Reservation> getAllReservations() {
        List<Reservation> reservations = new ArrayList<>();
        reservationRepository.findAll().forEach(reservations::add);
        return reservations;
    }
    @Override
    public Reservation getReservationById(Long id) {
        return reservationRepository.findById(id).orElse(null);
    }

    @Override
    public Reservation addReservation(Reservation reservation) {
        return reservationRepository.save(reservation);
    }

    @Override
    public Reservation saveReservation(Reservation reservation) {
        return reservationRepository.save(reservation);
    }

    @Override
    public Reservation updateReservation(Long id, Reservation reservation) {
        if (reservationRepository.existsById(id)) {
            reservation.setId(id);
            return reservationRepository.save(reservation);
        } else {
            return null; // ou lancez une exception si nécessaire
        }
    }

    @Override
    public void deleteReservation(Long id) {
        reservationRepository.deleteById(id);
    }

    @Override
    public List<ReservationDTO> getReservationsForCurrentUser() {
        // Obtenir l'utilisateur connecté
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();

        // Récupérer l'utilisateur complet (à travers un autre service ou repository)
        User currentUser = userRepository.findByEmail(currentUsername);

        // Vérifier si l'utilisateur existe
        if (currentUser == null) {
            throw new RuntimeException("Utilisateur non trouvé pour l'adresse email : " + currentUsername);
        }

        // Récupérer toutes les locations de voiture associées à l'utilisateur
        List<CarRental> carRentals = carRentalRepository.findByUser(currentUser);

        // Liste pour stocker toutes les réservations
        List<ReservationDTO> allReservations = new ArrayList<>();

        // Parcourir chaque location de voiture
        for (CarRental carRental : carRentals) {
            // Récupérer les réservations de chaque location de voiture
            List<Reservation> reservations = carRental.getReservations();

            // Transformer chaque réservation en DTO
            for (Reservation reservation : reservations) {
                ReservationDTO reservationDTO = ReservationMapper.toDto(reservation);
                allReservations.add(reservationDTO);
            }
        }

        return allReservations;
    }




}
