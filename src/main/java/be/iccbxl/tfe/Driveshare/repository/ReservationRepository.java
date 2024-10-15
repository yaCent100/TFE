package be.iccbxl.tfe.Driveshare.repository;

import be.iccbxl.tfe.Driveshare.DTO.CarReservationKpiDTO;
import be.iccbxl.tfe.Driveshare.DTO.ReservationDTO;
import be.iccbxl.tfe.Driveshare.model.Car;
import be.iccbxl.tfe.Driveshare.model.Reservation;
import be.iccbxl.tfe.Driveshare.model.User;
import io.micrometer.common.KeyValues;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {


    // Méthode pour récupérer les réservations en fonction des IDs des voitures
    @Query("SELECT r FROM Reservation r WHERE r.car.id IN :carIds")
    List<Reservation> findByCarIds(@Param("carIds") List<Long> carIds);

    @Query("SELECT r FROM Reservation r WHERE r.statut IN :statuses AND r.user = :user")
    List<Reservation> findByStatusesAndUser(@Param("statuses") List<String> statuses, @Param("user") User user);



    @Query("SELECT r FROM Reservation r WHERE r.statut = 'RESPONSE_PENDING'")
    List<Reservation> findManualRequests();

    @Query("SELECT r FROM Reservation r WHERE r.car.id IN :carIds AND r.car.user = :user AND r.statut = :statut")
    List<Reservation> findByCar_IdInAndCar_UserAndStatut(@Param("carIds") List<Long> carIds, @Param("user") User user, @Param("statut") String statut);
    List<Reservation> findByCar(Car car);

    //List<Reservation> findByStatus(String status);

    //long countByCarIdAndStatus(Long carId, String status);

    long countByCarId(Long carId);

    @Query("SELECT c.locality, COUNT(r) " +
            "FROM Reservation r JOIN r.car c " +
            "WHERE YEAR(r.createdAt) = :year AND MONTH(r.createdAt) = :month " +
            "GROUP BY c.locality")
    List<Object[]> countReservationsByLocalityAndDate(int year, int month);

    @Query("SELECT c.locality, COUNT(r) " +
            "FROM Reservation r JOIN r.car c " +
            "WHERE YEAR(r.createdAt) = :year " +
            "GROUP BY c.locality")
    List<Object[]> countReservationsByLocalityAndYear(int year);

    List<ReservationDTO> findByUser(User user);

    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.statut IN ('CONFIRMED', 'NOW', 'FINISHED')")
    long countConfirmedReservations();

    @Query("SELECT c FROM Car c JOIN c.reservations r JOIN r.payment pay WHERE r.statut = 'FINISHED' GROUP BY c.id ORDER BY COUNT(r) DESC")
    List<Car> findTop10MostReservedCars();


    @Query("SELECT r FROM Reservation r WHERE r.startLocation BETWEEN :startDate AND CURRENT_DATE AND r.statut IN ('CONFIRMED', 'NOW', 'FINISHED')")
    List<Reservation> findReservationsForLastMonth(@Param("startDate") LocalDate startDate);

    @Query("SELECT r FROM Reservation r WHERE r.startLocation BETWEEN :startDate AND :endDate AND r.statut IN ('CONFIRMED', 'NOW', 'FINISHED')")
    List<Reservation> findReservationsBetweenDates(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);


    long countByStatut(String now);

    @Query("SELECT MONTH(r.startLocation), COUNT(r) FROM Reservation r GROUP BY MONTH(r.startLocation)")
    List<Object[]> countReservationsByMonth();

    List<Reservation> findByStatutAndEndLocationBefore(String finished, LocalDateTime localDateTime);

    List<Reservation> findByCarId(Long carId);

    List<Reservation> findByStatut(String responsePending);

    @Query("SELECT r FROM Reservation r WHERE r.car.id = :carId AND r.statut IN ('confirmed', 'finished', 'now')")
    List<Reservation> findByCarIdAndStatus(@Param("carId") Long carId);
}
