package be.iccbxl.tfe.Driveshare.service;

import be.iccbxl.tfe.Driveshare.model.Car;


import java.time.LocalDate;

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

   /* List<Car> searchAvailableCars(String address, LocalDate dateDebut, LocalDate dateFin);*/
}
