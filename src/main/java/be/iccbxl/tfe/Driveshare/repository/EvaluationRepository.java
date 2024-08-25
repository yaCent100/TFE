package be.iccbxl.tfe.Driveshare.repository;

import be.iccbxl.tfe.Driveshare.model.Car;
import be.iccbxl.tfe.Driveshare.model.Evaluation;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EvaluationRepository extends CrudRepository<Evaluation, Long> {

    @Query(value = "SELECT e.reservation.car FROM Evaluation e WHERE e.note = 5")
    List<Car> findTop4CarsWithFiveStarRating();

    boolean existsByReservationId(Long reservationId);

    @Query("SELECT e FROM Evaluation e JOIN e.reservation r JOIN r.car c ORDER BY c.id, e.createdAt DESC")
    List<Evaluation> findAllGroupedByCar();

    @Query("SELECT e FROM Evaluation e WHERE e.reservation.car.id = :carId")
    List<Evaluation> findByCarId(@Param("carId") Long carId);
}