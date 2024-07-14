package be.iccbxl.tfe.Driveshare.repository;

import be.iccbxl.tfe.Driveshare.model.Car;
import be.iccbxl.tfe.Driveshare.model.CarRental;
import be.iccbxl.tfe.Driveshare.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CarRentalRepository extends JpaRepository<CarRental, Long> {

    List<CarRental> findByUser(User user);

    List<CarRental> findByCar(Car car);

    List<CarRental> findByCarIdIn(List<Long> carIds);



}
