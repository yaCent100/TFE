package be.iccbxl.tfe.Driveshare.controller;

import be.iccbxl.tfe.Driveshare.model.Car;
import be.iccbxl.tfe.Driveshare.service.serviceImpl.CarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class CarController {


    private final CarService carService;

    @Autowired
    public CarController(CarService carService) {
        this.carService = carService;
    }
    @GetMapping("/cars")
    public String getAllCars(Model model) {
        List<Car> cars = carService.getAllCars();
        model.addAttribute("cars", cars);
        return "car/index";
    }
}
