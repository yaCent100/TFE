package be.iccbxl.tfe.Driveshare.controller;

import be.iccbxl.tfe.Driveshare.model.Car;
import be.iccbxl.tfe.Driveshare.model.CarRental;
import be.iccbxl.tfe.Driveshare.model.Reservation;
import be.iccbxl.tfe.Driveshare.model.User;
import be.iccbxl.tfe.Driveshare.security.CustomUserDetail;
import be.iccbxl.tfe.Driveshare.service.serviceImpl.CarRentalService;
import be.iccbxl.tfe.Driveshare.service.serviceImpl.CarService;
import be.iccbxl.tfe.Driveshare.service.serviceImpl.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

@Controller
public class ReservationController {

    @Autowired
    private CarService carService;

    @Autowired
    private CarRentalService carRentalService;

    @Autowired
    private ReservationService reservationService;

    @GetMapping("/reservation/{id}")
    public String getReservationPage(@PathVariable Long id,
                                     @RequestParam String dateDebut,
                                     @RequestParam String dateFin,
                                     @AuthenticationPrincipal CustomUserDetail userDetail,
                                     Model model) {
        Car car = carService.getCarById(id);
        User user = userDetail.getUser();
        if (car == null) {
            return "redirect:/error";
        }

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate startDate = LocalDate.parse(dateDebut, formatter);
            LocalDate endDate = LocalDate.parse(dateFin, formatter);

            // Calculer la durée en jours
            long duration = ChronoUnit.DAYS.between(startDate, endDate);

            // Créer un CarRental
            CarRental carRental = new CarRental();
            carRental.setCar(car);
            carRental.setUser(user);
            carRental = carRentalService.save(carRental);

            // Créer une réservation
            Reservation reservation = new Reservation();
            reservation.setCarRental(carRental);
            reservation.setDebutLocation(startDate);
            reservation.setFinLocation(endDate);
            reservation.setStatut("en attente de paiement");
            reservation.setNbJours((int) duration);
            reservation.setCreatedAt(LocalDateTime.now());
            reservationService.saveReservation(reservation);

            // Formatter les dates pour affichage
            DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
            String formattedDateDebut = startDate.format(outputFormatter);
            String formattedDateFin = endDate.format(outputFormatter);

            // Préparer l'URL de l'image
            String carPhotoUrl;
            if (car.getPhotos() != null && !car.getPhotos().isEmpty()) {
                carPhotoUrl = "/uploads/" + car.getPhotos().get(0).getUrl();
            } else {
                carPhotoUrl = "/images/carDefault.jpg";
            }

            double totalAmount = car.getPrice().getMiddlePrice() * duration;

            model.addAttribute("car", car);
            model.addAttribute("user", user);
            model.addAttribute("dateDebut", formattedDateDebut);
            model.addAttribute("dateFin", formattedDateFin);
            model.addAttribute("nbJours", duration);
            model.addAttribute("carPhotoUrl", carPhotoUrl);
            model.addAttribute("reservationId", reservation.getId()); // Ajouter l'ID de réservation
            model.addAttribute("totalAmount", totalAmount);

            return "car/reservation";
        } catch (DateTimeParseException e) {
            return "redirect:/error";
        }
    }



    @GetMapping("/checkout")
    public String getCheckout(){
        return "checkout";
    }


    }




