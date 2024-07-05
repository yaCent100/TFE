package be.iccbxl.tfe.Driveshare.service.serviceImpl;

import be.iccbxl.tfe.Driveshare.model.Car;
import be.iccbxl.tfe.Driveshare.model.CarRental;
import be.iccbxl.tfe.Driveshare.repository.CarRentalRepository;
import be.iccbxl.tfe.Driveshare.service.CarRentalServiceI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CarRentalService implements CarRentalServiceI {

    @Autowired
    private CarRentalRepository carRentalRepository;

    @Override
    public CarRental getCarById(Long carId) {
        return carRentalRepository.findById(carId).orElse(null);
    }

    @Override
    public CarRental save(CarRental carRental) {
        return carRentalRepository.save(carRental);
    }

    @Override
    public List<CarRental> getAllCarRentals() {
        return carRentalRepository.findAll();
    }

}
