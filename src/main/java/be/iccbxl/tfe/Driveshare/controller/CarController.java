package be.iccbxl.tfe.Driveshare.controller;

import be.iccbxl.tfe.Driveshare.model.Car;
import be.iccbxl.tfe.Driveshare.model.Price;
import be.iccbxl.tfe.Driveshare.model.User;
import be.iccbxl.tfe.Driveshare.security.CustomUserDetail;
import be.iccbxl.tfe.Driveshare.service.serviceImpl.CarService;
import be.iccbxl.tfe.Driveshare.service.serviceImpl.CategoryService;
import be.iccbxl.tfe.Driveshare.service.serviceImpl.PriceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

@Controller
public class CarController {


    private final CarService carService;

    private final CategoryService categoryService;

    private final PriceService priceService;

    private static final Logger logger = LoggerFactory.getLogger(CarController.class);


    @Autowired
    public CarController(CarService carService, CategoryService categoryService, PriceService priceService, PriceService priceService1) {
        this.carService = carService;
        this.categoryService = categoryService;
        this.priceService = priceService1;
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



    // Contrôleur pour afficher les détails de la voiture
    @GetMapping("/cars/{id}")
    public String getCarById(
            @PathVariable Long id,
            @RequestParam(required = false) String dateDebut,
            @RequestParam(required = false) String dateFin,
            Model model,
            @AuthenticationPrincipal CustomUserDetail userDetail) { // Ajoutez ce modèle si vous avez configuré la méthode globale

        // Récupérer la voiture par son ID
        Car car = carService.getCarById(id);

        if (car == null) {
            return "error/404";
        }

        double averageRating = carService.calculateAverageRating(car);
        int totalEvaluations = (int) car.getReservations().stream()
                .filter(reservation -> reservation.getEvaluation() != null)
                .count();

        // Formatage des dates
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        String formattedDateDebut = "";
        String formattedDateFin = "";

        LocalDate localDateDebut = null;
        LocalDate localDateFin = null;

        String errorMessage = "";



        try {
            if (dateDebut != null && !dateDebut.isEmpty()) {
                localDateDebut = LocalDate.parse(dateDebut, DateTimeFormatter.ISO_LOCAL_DATE);
                formattedDateDebut = localDateDebut.format(formatter);
            }

            if (dateFin != null && !dateFin.isEmpty()) {
                localDateFin = LocalDate.parse(dateFin, DateTimeFormatter.ISO_LOCAL_DATE);
                formattedDateFin = localDateFin.format(formatter);
            }
        } catch (DateTimeParseException e) {
            e.printStackTrace();
            errorMessage = "Format de date invalide. Veuillez utiliser le format ISO (YYYY-MM-DD).";
        }

        Price price = car.getPrice();
        double dailyPrice = 0.0;
        double totalPrice = 0.0;

        if (price != null) {
            if (localDateDebut != null && localDateFin != null) {
                dailyPrice = priceService.calculateDisplayPrice(price, localDateDebut);
                long daysBetween = ChronoUnit.DAYS.between(localDateDebut, localDateFin) + 1; // Inclure le premier jour
                totalPrice = daysBetween * dailyPrice;
            } else {
                errorMessage = "Les dates de début et de fin doivent être fournies.";
            }
        }
        logger.info("Car ID: {}", id);
        logger.info("Display Price: {}", dailyPrice);

        String fullAddress = car.getAdresse() + ", " + (car.getPostalCode() != null ? car.getPostalCode() : "") + " " + (car.getLocality() != null ? car.getLocality() : "");
        model.addAttribute("fullAddress", fullAddress);

        logger.info("Fulladress: {}", fullAddress);


        model.addAttribute("car", car);
        model.addAttribute("averageRating", averageRating);
        model.addAttribute("totalEvaluations", totalEvaluations);
        model.addAttribute("dateDebut", formattedDateDebut);
        model.addAttribute("dateFin", formattedDateFin);
        model.addAttribute("displayPrice", dailyPrice);
        model.addAttribute("totalPrice", totalPrice);
        model.addAttribute("authenticatedUser", userDetail.getUser());


        model.addAttribute("errorMessage", errorMessage);

        return "car/details";
    }
















}
