package be.iccbxl.tfe.Driveshare.service;

import be.iccbxl.tfe.Driveshare.model.Car;

import java.util.List;

public interface CarsServiceI {

    List<Car> getAllCars();
    Car getCarById(Long id);
    Car saveCar(Car car);
    Car updateCar(Long id, Car car);
    void deleteCar(Long id);
}
