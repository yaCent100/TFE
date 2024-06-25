package be.iccbxl.tfe.Driveshare.repository;

import be.iccbxl.tfe.Driveshare.model.Indisponible;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface IndisponibleRepository extends JpaRepository<Indisponible, Long> {

    List<Indisponible> findByCarId(Long carId);
    List<Indisponible> findByCarIdAndStartDateAndEndDate(Long carId, LocalDate startDate, LocalDate EndDate);
}
