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
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class CarController {


    private final CarService carService;

    private final CategoryService categoryService;



    @Autowired
    public CarController(CarService carService, CategoryService categoryService, PriceService priceService) {
        this.carService = carService;
        this.categoryService = categoryService;
    }
   /* @GetMapping("/cars")
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
    }*/

    @GetMapping("/cars")
    public String getAllCars() {

        System.out.println("La méthode showNosLocationPage a été appelée");
        return "car/index";
    }



    @GetMapping("/cars/{id}")
    public String getCarById(
            @PathVariable Long id,
            @RequestParam(required = false) String dateDebut,
            @RequestParam(required = false) String dateFin,
            Model model) {

        // Récupérer la voiture par son ID
        Car car = carService.getCarById(id);

        // Vérifier si la voiture existe
        if (car == null) {
            // Gérer le cas où la voiture n'est pas trouvée
            return "error/404"; // Vue d'erreur personnalisée
        }

        // Calculer la note moyenne associée à cette voiture
        double averageRating = carService.calculateAverageRating(car);

        // Calculer le nombre total d'évaluations
        int totalEvaluations = (int) car.getReservations().stream()
                .filter(reservation -> reservation.getEvaluation() != null)
                .count();

        // Formatter les dates
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        String formattedDateDebut = "";
        String formattedDateFin = "";

        try {
            if (dateDebut != null && !dateDebut.isEmpty()) {
                LocalDate localDateDebut = LocalDate.parse(dateDebut, DateTimeFormatter.ISO_LOCAL_DATE);
                formattedDateDebut = localDateDebut.format(formatter);
            }
            if (dateFin != null && !dateFin.isEmpty()) {
                LocalDate localDateFin = LocalDate.parse(dateFin, DateTimeFormatter.ISO_LOCAL_DATE);
                formattedDateFin = localDateFin.format(formatter);
            }
        } catch (DateTimeParseException e) {
            // Gérer l'exception de parsing
            e.printStackTrace();
        }

        // Ajouter les attributs au modèle
        model.addAttribute("car", car);
        model.addAttribute("averageRating", averageRating);
        model.addAttribute("totalEvaluations", totalEvaluations);
        model.addAttribute("dateDebut", formattedDateDebut);
        model.addAttribute("dateFin", formattedDateFin);

        return "car/details"; // Vue pour afficher les détails de la voiture
    }










}
