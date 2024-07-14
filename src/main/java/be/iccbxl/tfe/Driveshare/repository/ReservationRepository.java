package be.iccbxl.tfe.Driveshare.repository;

import be.iccbxl.tfe.Driveshare.model.CarRental;
import be.iccbxl.tfe.Driveshare.model.Reservation;
import be.iccbxl.tfe.Driveshare.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByCarRental(CarRental carRental);

    List<Reservation> findByCarRentalIn(List<CarRental> carRentals);
    List<Reservation> findByCarRentalInAndStatutIn(List<CarRental> carRentals, List<String> statuses);

    @Query("SELECT r FROM Reservation r WHERE r.carRental.id IN :carRentalIds")
    List<Reservation> findByCarRentalIds(@Param("carRentalIds") List<Long> carRentalIds);

    @Query("SELECT r FROM Reservation r WHERE r.statut IN :statuses AND r.carRental.user = :user")
    List<Reservation> findByStatusesAndUser(@Param("statuses") List<String> statuses, @Param("user") User user);

    List<Reservation> findByCarRentalCarIdIn(List<Long> carIds);


}
