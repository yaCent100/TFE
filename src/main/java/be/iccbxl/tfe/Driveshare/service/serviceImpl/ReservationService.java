package be.iccbxl.tfe.Driveshare.service.serviceImpl;

import be.iccbxl.tfe.Driveshare.DTO.ReservationDTO;
import be.iccbxl.tfe.Driveshare.DTO.CarMapper;
import be.iccbxl.tfe.Driveshare.model.Car;
import be.iccbxl.tfe.Driveshare.model.Reservation;
import be.iccbxl.tfe.Driveshare.model.User;
import be.iccbxl.tfe.Driveshare.repository.CarRepository;
import be.iccbxl.tfe.Driveshare.repository.ReservationRepository;
import be.iccbxl.tfe.Driveshare.repository.UserRepository;
import be.iccbxl.tfe.Driveshare.service.ReservationServiceI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.chrono.ChronoLocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReservationService implements ReservationServiceI {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private UserRepository userRepository;

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

        // Récupérer toutes les voitures associées à l'utilisateur
        List<Car> userCars = carRepository.findByUser(currentUser);

        // Liste pour stocker toutes les réservations
        List<ReservationDTO> allReservations = new ArrayList<>();

        // Parcourir chaque voiture de l'utilisateur pour récupérer les réservations associées
        for (Car car : userCars) {
            List<Reservation> reservations = reservationRepository.findByCar(car);

            // Transformer chaque réservation en DTO
            for (Reservation reservation : reservations) {
                ReservationDTO reservationDTO = CarMapper.toReservationDTO(reservation);
                allReservations.add(reservationDTO);
            }
        }

        return allReservations;
    }




    @Override
    public List<Reservation> getReservationsByCarIds(List<Long> carIds) {
        return reservationRepository.findByCarIds(carIds);
    }

    @Override
    public List<Reservation> getReservationsByStatusesAndUser(List<String> statuses, User user) {
        return reservationRepository.findByStatusesAndUser(statuses, user);
    }

    // Méthode planifiée pour s'exécuter toutes les minutes (pour les tests)
    @Transactional
    @Scheduled(cron = "0 0 0 * * ?") // Exécuter tous les jours à minuit
    public void updateReservationStatuses() {
        LocalDateTime now = LocalDateTime.now();
        System.out.println("Executing scheduled task at " + now);

        List<Reservation> reservations = reservationRepository.findAll();
        System.out.println("Found " + reservations.size() + " reservations to update.");

        for (Reservation reservation : reservations) {
            LocalDate debutLocationDate = reservation.getDebutLocation();
            LocalDate finLocationDate = reservation.getFinLocation();

            LocalDateTime debutLocation = debutLocationDate.atStartOfDay();
            LocalDateTime finLocation = finLocationDate.atTime(LocalTime.MAX);

            if (reservation.getStatut().equals("PAYMENT_PENDING") && now.isAfter(debutLocation)) {
                reservation.setStatut("CANCELLED");
            } else if (reservation.getStatut().equals("CONFIRMED") && now.isAfter(debutLocation) && now.isBefore(finLocation)) {
                reservation.setStatut("NOW");
            } else if (reservation.getStatut().equals("CONFIRMED") && now.isAfter(finLocation)) {
                reservation.setStatut("FINISHED");
            }

            reservationRepository.save(reservation);
        }

        System.out.println("Updated " + reservations.size() + " reservations to appropriate statuses.");
    }


    public void cancelReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId).orElse(null);
        if (reservation != null) {
            reservation.setStatut("CANCELLED");
            reservationRepository.save(reservation);
        }
    }

    public void acceptReservation(Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Réservation non trouvée"));
        reservation.setStatut("CONFIRMED");
        reservationRepository.save(reservation);
    }

    public void rejectReservation(Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Réservation non trouvée"));
        reservation.setStatut("REJECTED");
        reservationRepository.save(reservation);
    }

    @Scheduled(fixedRate = 3600000) // Exécute toutes les heures
    public void cancelExpiredManualRequests() {
        LocalDateTime now = LocalDateTime.now();
        List<Reservation> manualRequests = reservationRepository.findManualRequests();
        for (Reservation reservation : manualRequests) {
            if (reservation.getCreatedAt().isBefore(now.minusHours(48)) && "RESPONSE_PENDING".equals(reservation.getStatut())) {
                reservation.setStatut("CANCELLED");
                reservationRepository.save(reservation);
            }
        }
    }

    public List<Reservation> findManualRequestsByUserAndCarIds(User user, List<Long> carIds) {
        return reservationRepository.findByCar_IdInAndCar_UserAndStatut(carIds, user, "RESPONSE_PENDING");
    }



    public List<Object[]> getReservationCountByLocality(int year, int month) {
        return reservationRepository.countReservationsByLocalityAndDate(year, month);
    }

    public List<Object[]> getReservationCountByLocality(int year) {
        return reservationRepository.countReservationsByLocalityAndYear(year);
    }

   /* public List<Reservation> findByStatus(String status) {
        return reservationRepository.findByStatus(status);
    }*/

    // Calcul du taux d'acceptation
  /*  public double calculateAcceptanceRate() {
        long totalReservations = reservationRepository.count(); // Total des réservations
       /* long acceptedReservations = reservationRepository.countByStatus("accepted"); // Réservations acceptées

        return totalReservations == 0 ? 0 : (double) acceptedReservations / totalReservations * 100;
    }*/

}
