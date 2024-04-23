package be.iccbxl.tfe.Driveshare.service;

import be.iccbxl.tfe.Driveshare.model.Car;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface CarServiceI {

    List<Car> getAllCars();
    Car getCarById(Long id);
    Car addUser(Car car);

    Car saveCar(Car car);

    Car updateCar(Long id, Car car);
    void deleteCar(Long id);
    double calculateAverageRating(Car car);


    Map<Long, Double> getAverageRatingsForCars();

    Map<Long, Integer> getReviewCountsForCars();
}
