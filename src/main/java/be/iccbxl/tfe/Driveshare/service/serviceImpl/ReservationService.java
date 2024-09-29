package be.iccbxl.tfe.Driveshare.service.serviceImpl;

import be.iccbxl.tfe.Driveshare.DTO.*;
import be.iccbxl.tfe.Driveshare.model.Car;
import be.iccbxl.tfe.Driveshare.model.Reservation;
import be.iccbxl.tfe.Driveshare.model.User;
import be.iccbxl.tfe.Driveshare.repository.CarRepository;
import be.iccbxl.tfe.Driveshare.repository.ReservationRepository;
import be.iccbxl.tfe.Driveshare.repository.UserRepository;
import be.iccbxl.tfe.Driveshare.service.ReservationServiceI;
import jakarta.persistence.EntityManager;
import org.springdoc.api.OpenApiResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReservationService implements ReservationServiceI {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CarRepository carRepository;

    @Autowired
    private EntityManager entityManager;

    @Override
    public List<Reservation> getAllReservations() {
        List<Reservation> reservations = new ArrayList<>();
        reservationRepository.findAll().forEach(reservations::add);
        return reservations;
    }

    @Override
    public List<ReservationDTO> getAllReservationsDTOs() {
        List<Reservation> reservations = reservationRepository.findAll();
        if (reservations == null) {
            return new ArrayList<>();
        }
        return reservations.stream()
                .map(MapperDTO::toReservationDTO)
                .collect(Collectors.toList());
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
                ReservationDTO reservationDTO = MapperDTO.toReservationDTO(reservation);
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
            LocalDate debutLocationDate = reservation.getStartLocation();
            LocalDate finLocationDate = reservation.getEndLocation();

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

    @Override
    public List<ReservationDTO> getReservationsByUser(User user) {
        return reservationRepository.findByUser(user);
    }

    public List<ReservationDTO> getUserGains(User user) {
        List<ReservationDTO> reservations = reservationRepository.findByUser(user);

        return reservations.stream()
                .filter(reservation -> reservation.getPayment() != null && reservation.getPayment().getPrixPourUser() > 0)
                .collect(Collectors.toList());
    }

    public ReservationDTO getReservationDetails(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new OpenApiResourceNotFoundException("Reservation not found"));
        return MapperDTO.toReservationDTO(reservation);
    }

    public long getTotalConfirmedReservations() {
        return reservationRepository.countConfirmedReservations();
    }

    public List<CarReservationKpiDTO> getTop10MostReservedCarsThisMonth() {
        // Récupérer toutes les voitures
        List<Car> cars = reservationRepository.findTop10MostReservedCarsThisMonth();

        // Calculer les KPI pour chaque voiture, trier par nombre de réservations et limiter à 10
        return cars.stream()
                .map(car -> {
                    long reservationCount = car.getReservations().size(); // Nombre de réservations
                    BigDecimal totalRevenue = car.getReservations().stream()
                            .filter(r -> r.getPayment() != null)
                            .map(r -> BigDecimal.valueOf(r.getPayment().getPrixTotal())) // Conversion du double en BigDecimal
                            .reduce(BigDecimal.ZERO, BigDecimal::add); // Additionne toutes les valeurs BigDecimal

                    // Calculer la tendance (positif, négatif)
                    int trend = calculateTrend(car);

                    // Mapper à DTO
                    return CarReservationKpiDTO.toCarReservationKpiDTO(car, reservationCount, totalRevenue, trend);
                })
                .sorted(Comparator.comparingLong(CarReservationKpiDTO::getReservationCount).reversed()) // Trier par nombre de réservations décroissant
                .limit(10) // Limiter aux 10 voitures les plus réservées
                .collect(Collectors.toList());
    }


    private int calculateTrend(Car car) {
        // Logique pour calculer la tendance
        // Exemple simplifié : comparer les réservations du mois actuel et du mois précédent
        long currentMonthReservations = car.getReservations().stream()
                .filter(r -> r.getStartLocation().getMonthValue() == LocalDate.now().getMonthValue())
                .count();
        long previousMonthReservations = car.getReservations().stream()
                .filter(r -> r.getEndLocation().getMonthValue() == LocalDate.now().minusMonths(1).getMonthValue())
                .count();

        return Long.compare(currentMonthReservations, previousMonthReservations); // 1 = up, -1 = down
    }

    public List<Reservation> getReservationsForLastMonth() {
        LocalDate oneMonthAgo = LocalDate.now().minusMonths(1);
        return reservationRepository.findReservationsForLastMonth(oneMonthAgo);
    }

    public List<Reservation> getReservationsForPreviousMonth() {
        // Obtenir la date du premier et dernier jour du mois précédent
        LocalDate firstDayOfLastMonth = LocalDate.now().minusMonths(1).withDayOfMonth(1);
        LocalDate lastDayOfLastMonth = LocalDate.now().minusMonths(1).withDayOfMonth(firstDayOfLastMonth.lengthOfMonth());

        // Récupérer les réservations pour cette plage de dates
        return reservationRepository.findReservationsBetweenDates(firstDayOfLastMonth, lastDayOfLastMonth);
    }



    public List<UserReservationKpiDTO> getTop10UsersByReservationsForCurrentMonth() {
        // Récupérer toutes les réservations du mois en cours
        List<Reservation> currentMonthReservations = getReservationsForLastMonth(); // ou getReservationsForCurrentMonth si c'est le mois actuel

        // Récupérer toutes les réservations du mois précédent
        List<Reservation> lastMonthReservations = getReservationsForPreviousMonth();

        // Grouper les réservations par utilisateur et compter le nombre de réservations pour le mois en cours
        Map<User, Long> currentMonthReservationCountMap = currentMonthReservations.stream()
                .collect(Collectors.groupingBy(Reservation::getUser, Collectors.counting()));

        // Grouper les réservations par utilisateur et compter le nombre de réservations pour le mois précédent
        Map<User, Long> lastMonthReservationCountMap = lastMonthReservations.stream()
                .collect(Collectors.groupingBy(Reservation::getUser, Collectors.counting()));

        // Transformer la map en liste de DTOs avec la tendance calculée
        List<UserReservationKpiDTO> userReservationKpiDTOs = currentMonthReservationCountMap.entrySet().stream()
                .map(entry -> {
                    User user = entry.getKey();
                    Long currentMonthCount = entry.getValue();
                    Long lastMonthCount = lastMonthReservationCountMap.getOrDefault(user, 0L);

                    // Calculer la tendance (par exemple : augmentation, diminution ou stable)
                    String trend;
                    if (currentMonthCount > lastMonthCount) {
                        trend = "Up";
                    } else if (currentMonthCount < lastMonthCount) {
                        trend = "Down";
                    } else {
                        trend = "Stable";
                    }

                    // Créer un DTO avec les informations et la tendance
                    return new UserReservationKpiDTO(
                            user.getId(),
                            user.getFirstName(),
                            user.getLastName(),
                            currentMonthCount,
                            user.getPhotoUrl(),
                            trend
                    );
                })
                .toList();

        // Trier par nombre de réservations décroissant et retourner les 10 premiers
        return userReservationKpiDTOs.stream()
                .sorted((u1, u2) -> Long.compare(u2.getReservationCount(), u1.getReservationCount()))
                .limit(10)
                .collect(Collectors.toList());
    }





    public List<ReservationDTO> getReservationsForCar(Long carId) {
        // Récupérer uniquement les réservations avec les statuts pertinents
        return reservationRepository.findByCarIdAndStatus(carId).stream()
                .map(MapperDTO::toReservationDTO)
                .collect(Collectors.toList());
    }


    @Scheduled(fixedRate = 3600000) // Exécuter la tâche toutes les heures (3600000 ms = 1 heure)
    public void updateExpiredReservations() {
        List<Reservation> pendingReservations = reservationRepository.findByStatut("RESPONSE_PENDING");
        LocalDateTime now = LocalDateTime.now();

        for (Reservation reservation : pendingReservations) {
            if (reservation.getCreatedAt().plusHours(48).isBefore(now)) {
                reservation.setStatut("EXPIRED");
                reservationRepository.save(reservation);
            }
        }
    }

    public List<Map<String, Object>> getConfirmedReservationsByMonth() {
        LocalDate startDate = LocalDate.now().minusYears(1).withDayOfMonth(1);  // Date d'il y a 12 mois
        LocalDate endDate = LocalDate.now().withDayOfMonth(1);  // Date actuelle au début du mois

        // Obtenir les résultats des réservations confirmées depuis la base de données
        List<Object[]> results = entityManager.createQuery(
                        "SELECT MONTH(r.createdAt), YEAR(r.createdAt), COUNT(r.id) " +
                                "FROM Reservation r " +
                                "WHERE r.statut IN ('FINISHED', 'CONFIRMED', 'NOW') " +
                                "AND r.createdAt >= :startDate " +
                                "GROUP BY YEAR(r.createdAt), MONTH(r.createdAt) " +
                                "ORDER BY YEAR(r.createdAt), MONTH(r.createdAt)"
                )
                .setParameter("startDate", startDate.atStartOfDay())
                .getResultList();

        // Créer une map des résultats avec le mois et l'année
        Map<String, Long> reservationsMap = new HashMap<>();
        for (Object[] result : results) {
            String monthYear = result[1] + "-" + String.format("%02d", result[0]);  // Format : "YYYY-MM"
            reservationsMap.put(monthYear, (Long) result[2]);  // Stocker le count
        }

        // Créer la liste de réponse avec tous les mois des 12 derniers mois
        List<Map<String, Object>> reservationsData = new ArrayList<>();
        LocalDate currentMonth = startDate;
        while (!currentMonth.isAfter(endDate)) {
            String monthYear = currentMonth.getYear() + "-" + String.format("%02d", currentMonth.getMonthValue());

            Map<String, Object> data = new HashMap<>();
            data.put("month", currentMonth.getMonthValue());
            data.put("year", currentMonth.getYear());
            data.put("count", reservationsMap.getOrDefault(monthYear, 0L));  // Mettre 0 si pas de réservation

            reservationsData.add(data);
            currentMonth = currentMonth.plusMonths(1);  // Passer au mois suivant
        }

        return reservationsData;
    }


}
