package be.iccbxl.tfe.Driveshare.service.serviceImpl;

import be.iccbxl.tfe.Driveshare.DTO.ReservationDTO;
import be.iccbxl.tfe.Driveshare.DTO.CarMapper;
import be.iccbxl.tfe.Driveshare.model.Car;
import be.iccbxl.tfe.Driveshare.model.CarRental;
import be.iccbxl.tfe.Driveshare.model.Reservation;
import be.iccbxl.tfe.Driveshare.model.User;
import be.iccbxl.tfe.Driveshare.repository.CarRentalRepository;
import be.iccbxl.tfe.Driveshare.repository.CarRepository;
import be.iccbxl.tfe.Driveshare.repository.ReservationRepository;
import be.iccbxl.tfe.Driveshare.repository.UserRepository;
import be.iccbxl.tfe.Driveshare.service.ReservationServiceI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ReservationService implements ReservationServiceI {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CarRentalRepository carRentalRepository;
    @Autowired
    private CarRepository carRepository;

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
        List<Car> userCars = carRepository.findByUser(currentUser);

        // Liste pour stocker toutes les réservations
        List<ReservationDTO> allReservations = new ArrayList<>();

        // Parcourir chaque voiture de l'utilisateur
        for (Car car : userCars) {
            // Récupérer les locations de voiture (UserCarRental) associées à chaque voiture
            List<CarRental> carRentals = carRentalRepository.findByCar(car);

            // Récupérer les réservations associées à chaque location de voiture
            for (CarRental carRental : carRentals) {
                List<Reservation> reservations = reservationRepository.findByCarRental(carRental);

                // Transformer chaque réservation en DTO
                for (Reservation reservation : reservations) {
                    ReservationDTO reservationDTO = CarMapper.toReservationDTO(reservation);
                    allReservations.add(reservationDTO);
                }
            }
        }

        return allReservations;
    }



    @Override
    public List<Reservation> getReservationsByCarRentals(List<CarRental> carRentals) {
        return reservationRepository.findByCarRentalIn(carRentals);
    }

    @Override
    public List<Reservation> getReservationsByCarRentalsAndStatuses(List<CarRental> carRentals, List<String> statuses) {
        return null;
    }

    @Override
    public List<Reservation> getReservationsByCarIds(List<Long> carIds) {
        return reservationRepository.findByCarRentalCarIdIn(carIds);
    }

    @Override
    public List<Reservation> getReservationsByStatusesAndUser(List<String> statuses, User user) {
        return reservationRepository.findByStatusesAndUser(statuses, user);
    }





}
