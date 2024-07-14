package be.iccbxl.tfe.Driveshare.service;

import be.iccbxl.tfe.Driveshare.model.Car;
import be.iccbxl.tfe.Driveshare.model.CarRental;

import java.util.List;

public interface CarRentalServiceI {
    CarRental getCarById(Long carId);

    CarRental save(CarRental carRental);

    List<CarRental> getAllCarRentals();

    List<CarRental> getCarById(List<Long> ids);

    List<CarRental> getCarRentalsByCarIds(List<Long> carIds);



}
