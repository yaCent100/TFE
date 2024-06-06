package be.iccbxl.tfe.Driveshare.controller;

import be.iccbxl.tfe.Driveshare.model.Car;
import be.iccbxl.tfe.Driveshare.model.CarRental;
import be.iccbxl.tfe.Driveshare.model.Reservation;
import be.iccbxl.tfe.Driveshare.model.User;
import be.iccbxl.tfe.Driveshare.security.CustomUserDetail;
import be.iccbxl.tfe.Driveshare.service.serviceImpl.CarService;
import be.iccbxl.tfe.Driveshare.service.serviceImpl.PriceService;
import be.iccbxl.tfe.Driveshare.service.serviceImpl.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;


@Controller
public class AccountController {

    private final UserService userService;
    private final PriceService priceService;

    @Autowired
    public AccountController(UserService userService, PriceService priceService) {
        this.userService = userService;
        this.priceService = priceService;
    }


    @GetMapping("/account")
    public String getAccount(Model model, @AuthenticationPrincipal CustomUserDetail userDetails) {
        int confirmedCount = 0; // Initialisez le compteur de réservations confirmées à 0
        int carCount = 0; // Initialisez le compteur de voitures à 0

        if (userDetails != null) {
            User user = userDetails.getUser();

            // Itérez sur les réservations de l'utilisateur pour compter les réservations confirmées
            for (CarRental carRental : user.getCarRentals()) {
                for (Reservation reservation : carRental.getReservations()) {
                    if ("confirmé".equals(reservation.getStatut())) {
                        confirmedCount++;
                    }
                }
            }

            // Récupérez le nombre de voitures liées à l'utilisateur depuis la base de données
            carCount = Math.toIntExact(userService.countCarsById(user.getId())); // Suppose que vous avez un repository pour les voitures

            // Ajoutez les compteurs au modèle
            model.addAttribute("confirmedCount", confirmedCount);
            model.addAttribute("carCount", carCount);
            model.addAttribute("user", user);
        }
        return "account/index";
    }

    @GetMapping("/account/cars")
    public String getCars(Model model, @AuthenticationPrincipal CustomUserDetail userDetails) {
        if (userDetails != null) {
            User user = userDetails.getUser();

            List<Car> userCars = userService.getUserById(user.getId()).getOwnedCars();
            LocalDate today = LocalDate.now(); // Date actuelle pour déterminer la saison

            // Calculer le prix pour chaque voiture et l'assigner

            // Ajouter les voitures au modèle
            model.addAttribute("user", user);
            model.addAttribute("cars", userCars);

            return "account/cars";
        } else {
            // Gérer le cas où aucun utilisateur n'est connecté
            return "redirect:/login"; // Rediriger vers la page de connexion
        }
    }

    @GetMapping("/voitures/{id}")
    public String getCarDetails(@PathVariable("id") Long carId, Model model, @AuthenticationPrincipal CustomUserDetail userDetails) {
        // Vérifier si l'utilisateur est connecté
        if (userDetails != null) {
            // Récupérer l'utilisateur connecté
            User user = userDetails.getUser();

            // Récupérer la liste des voitures de l'utilisateur
            List<Car> userCars = user.getOwnedCars();

            // Rechercher la voiture par ID dans la liste des voitures de l'utilisateur
            Optional<Car> optionalCar = userCars.stream().filter(car -> car.getId().equals(carId)).findFirst();

            // Vérifier si la voiture avec l'ID spécifié existe pour cet utilisateur
            if (optionalCar.isPresent()) {
                Car car = optionalCar.get();

                // Ajouter les détails de la voiture au modèle
                model.addAttribute("car", car);
                model.addAttribute("user", user);

                return "account/car-show"; // Page des détails de la voiture
            } else {
                // Gérer le cas où la voiture n'est pas trouvée pour cet utilisateur
                return "redirect:/voitures"; // Rediriger vers la liste des voitures de l'utilisateur
            }
        } else {
            // Gérer le cas où aucun utilisateur n'est connecté
            return "redirect:/login"; // Rediriger vers la page de connexion
        }
    }


}
