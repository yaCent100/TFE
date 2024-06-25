package be.iccbxl.tfe.Driveshare.service;

import be.iccbxl.tfe.Driveshare.DTO.CarDTO;
import be.iccbxl.tfe.Driveshare.model.Car;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;


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



    @Transactional(readOnly = true)
    List<CarDTO> searchAvailableCars(String address, double lat, double lng, LocalDate dateDebut, LocalDate dateFin) throws Exception;

    Car createCar(CarDTO carDTO);


    void updateCar(Long id,CarDTO carDTO);

}
