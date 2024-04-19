package be.iccbxl.tfe.Driveshare.service;

import be.iccbxl.tfe.Driveshare.model.Car;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CarServiceI {

    List<Car> getAllCars();
    Car getCarById(Long id);
    Car addUser(Car car);

    Car saveCar(Car car);

    Car updateCar(Long id, Car car);
    void deleteCar(Long id);
}
