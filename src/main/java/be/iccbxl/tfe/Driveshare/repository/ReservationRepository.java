package be.iccbxl.tfe.Driveshare.repository;

import be.iccbxl.tfe.Driveshare.model.CarRental;
import be.iccbxl.tfe.Driveshare.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByCarRental(CarRental carRental);

}
