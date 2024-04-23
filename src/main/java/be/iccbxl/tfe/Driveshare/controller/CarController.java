package be.iccbxl.tfe.Driveshare.controller;

import be.iccbxl.tfe.Driveshare.model.Car;
import be.iccbxl.tfe.Driveshare.model.Category;
import be.iccbxl.tfe.Driveshare.service.serviceImpl.CarService;
import be.iccbxl.tfe.Driveshare.service.serviceImpl.CategoryService;
import be.iccbxl.tfe.Driveshare.service.serviceImpl.EvaluationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Map;

@Controller
public class CarController {


    private final CarService carService;

    private final CategoryService categoryService;


    @Autowired
    public CarController(CarService carService, CategoryService categoryService) {
        this.carService = carService;
        this.categoryService = categoryService;
    }
    @GetMapping("/cars")
    public String getAllCars(Model model) {
        List<Car> cars = carService.getAllCars();
        List<Category> categories = categoryService.getAllCategory();
        Map<Long, Double> averageRatings = carService.getAverageRatingsForCars();
        Map<Long, Integer> reviewCounts = carService.getReviewCountsForCars();

        model.addAttribute("cars", cars);
        model.addAttribute("categories", categories);
        model.addAttribute("averageRatings", averageRatings);
        model.addAttribute("reviewCounts", reviewCounts);

        return "car/index";
    }


    @GetMapping("/cars/{id}")
    public String getCarById(@PathVariable Long id, Model model) {
        // Récupérer la voiture par son ID
        Car car = carService.getCarById(id);

        // Vérifier si la voiture existe
        if (car == null) {
            // Gérer le cas où la voiture n'est pas trouvée, par exemple en renvoyant une erreur 404
            return "error/404"; // à adapter selon votre gestion d'erreurs
        }

        // Calculer la note moyenne associée à cette voiture
        double averageRating = carService.calculateAverageRating(car);

        // Ajouter la voiture et la note moyenne au modèle
        model.addAttribute("car", car);
        model.addAttribute("averageRating", averageRating);

        return "car/details"; // à adapter selon le nom de votre vue
    }




}
