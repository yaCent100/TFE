package be.iccbxl.tfe.Driveshare.controller;

import be.iccbxl.tfe.Driveshare.model.Car;
import be.iccbxl.tfe.Driveshare.service.serviceImpl.CarService;
import be.iccbxl.tfe.Driveshare.service.serviceImpl.EvaluationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Controller
public class CarController {


    private final CarService carService;
    private final EvaluationService evaluationService;

    @Autowired
    public CarController(CarService carService, EvaluationService evaluationService) {
        this.carService = carService;
        this.evaluationService = evaluationService;
    }
    @GetMapping("/cars")
    public String getAllCars(Model model) {
        List<Car> cars = carService.getAllCars();
        model.addAttribute("cars", cars);
        return "car/index";
    }

    @GetMapping("/cars/{id}")
    public String getCarById(@PathVariable Long id, Model model) {
        Car car = carService.getCarById(id);
        double averageRating = evaluationService.calculateAverageRating(car);
        model.addAttribute("car", car);
        model.addAttribute("averageRating", averageRating);
        return "car/details";
    }
}
