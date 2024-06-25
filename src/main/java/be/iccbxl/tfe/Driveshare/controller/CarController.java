package be.iccbxl.tfe.Driveshare.controller;

import be.iccbxl.tfe.Driveshare.model.Car;
import be.iccbxl.tfe.Driveshare.model.Category;
import be.iccbxl.tfe.Driveshare.model.User;
import be.iccbxl.tfe.Driveshare.security.CustomUserDetail;
import be.iccbxl.tfe.Driveshare.service.serviceImpl.CarService;
import be.iccbxl.tfe.Driveshare.service.serviceImpl.CategoryService;
import be.iccbxl.tfe.Driveshare.service.serviceImpl.PriceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class CarController {


    private final CarService carService;

    private final CategoryService categoryService;

    private final PriceService priceService;


    @Autowired
    public CarController(CarService carService, CategoryService categoryService, PriceService priceService) {
        this.carService = carService;
        this.categoryService = categoryService;
        this.priceService = priceService;
    }
    @GetMapping("/cars")
    public String getAllCars(Model model, @AuthenticationPrincipal CustomUserDetail userDetails) {
        User user = userDetails.getUser();

        List<Car> cars = carService.getAllCars();
        List<Category> categories = categoryService.getAllCategory();
        Map<Long, Double> averageRatings = carService.getAverageRatingsForCars();
        Map<Long, Integer> reviewCounts = carService.getReviewCountsForCars();

        // Récupérer l'utilisateur actuel

        model.addAttribute("user", user);
        model.addAttribute("cars", cars);
        model.addAttribute("categories", categories);
        model.addAttribute("averageRatings", averageRatings);
        model.addAttribute("reviewCounts", reviewCounts);

        return "car/index";
    }



    @GetMapping("/cars/{id}")
    public String getCarById(@PathVariable Long id,@AuthenticationPrincipal CustomUserDetail userDetails, Model model) {
        User user = userDetails.getUser();


        // Récupérer la voiture par son ID
        Car car = carService.getCarById(id);

        // Vérifier si la voiture existe
        if (car == null) {
            // Gérer le cas où la voiture n'est pas trouvée
            return "error/404"; // Vue d'erreur personnalisée
        }



        // Calculer la note moyenne associée à cette voiture
        double averageRating = carService.calculateAverageRating(car);
        int totalEvaluations = car.getCarRentals().stream()
                .mapToInt(rental -> rental.getEvaluations().size())
                .sum();

        model.addAttribute("user", user);

        // Ajouter la voiture, la note moyenne, et le prix calculé au modèle pour l'affichage
        model.addAttribute("car", car);
        model.addAttribute("averageRating", averageRating);
        model.addAttribute("totalEvaluations", totalEvaluations);



        return "car/details"; // Nom de la vue pour afficher les détails de la voiture
    }








}
