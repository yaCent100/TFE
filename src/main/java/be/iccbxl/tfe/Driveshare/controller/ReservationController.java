package be.iccbxl.tfe.Driveshare.controller;

import be.iccbxl.tfe.Driveshare.DTO.ReservationDTO;
import be.iccbxl.tfe.Driveshare.model.*;
import be.iccbxl.tfe.Driveshare.security.CustomUserDetail;
import be.iccbxl.tfe.Driveshare.service.serviceImpl.CarService;
import be.iccbxl.tfe.Driveshare.service.serviceImpl.ReservationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private ReservationService reservationService;

    @GetMapping("/reservation/{id}")
    public String getReservationPage(@PathVariable Long id,
                                     @RequestParam String dateDebut,
                                     @RequestParam String dateFin,
                                     @RequestParam(required = false, defaultValue = "basic") String insurance,
                                     @AuthenticationPrincipal CustomUserDetail userDetail,
                                     Model model) {
        Logger logger = LoggerFactory.getLogger(ReservationController.class);

        Car car = carService.getCarById(id);
        User user = userDetail.getUser();
        if (car == null) {
            return "redirect:/error";
        }

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate startDate = LocalDate.parse(dateDebut, formatter);
            LocalDate endDate = LocalDate.parse(dateFin, formatter);

            long duration = ChronoUnit.DAYS.between(startDate, endDate);
            logger.info("Duration: " + duration);

            Reservation reservation = new Reservation();
            reservation.setCar(car);
            reservation.setUser(user);
            reservation.setStartLocation(startDate);
            reservation.setEndLocation(endDate);
            reservation.setStatut("PAYMENT_PENDING");
            reservation.setNbJours((int) duration);
            reservation.setCreatedAt(LocalDateTime.now());
            reservation.setInsurance(insurance);
            reservationService.saveReservation(reservation);

            DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
            String formattedDateDebut = startDate.format(outputFormatter);
            String formattedDateFin = endDate.format(outputFormatter);

            String carPhotoUrl;
            if (car.getPhotos() != null && !car.getPhotos().isEmpty()) {
                carPhotoUrl = "/uploads/" + car.getPhotos().get(0).getUrl();
            } else {
                carPhotoUrl = "/images/carDefault.jpg";
            }

            double basePrice = car.getPrice().getMiddlePrice();
            double totalAmount = basePrice * duration;

            Price carPrice = car.getPrice();
            double promo1 = carPrice.getPromo1();
            double promo2 = carPrice.getPromo2();

            double promotion = 0;
            if (duration >= 30) {
                promotion = promo2;
            } else if (duration > 14) {
                promotion = promo1;
            }

            double discountAmount = totalAmount * (promotion / 100);
            double totalAmountAfterDiscount = totalAmount - discountAmount;

            double insuranceCost = 0;
            switch (insurance) {
                case "mini-omnium":
                    insuranceCost = 2 * duration;
                    break;
                case "omnium":
                    insuranceCost = 5 * duration;
                    break;
                case "basic":
                default:
                    insuranceCost = 0;
                    break;
            }

            double totalWithInsurance = totalAmountAfterDiscount + insuranceCost;

            logger.info("Base Price: " + basePrice);
            logger.info("Total Amount: " + totalAmount);
            logger.info("Promotion: " + promotion + "%");
            logger.info("Discount Amount: " + discountAmount);
            logger.info("Total Amount After Discount: " + totalAmountAfterDiscount);
            logger.info("Insurance: " + insurance);
            logger.info("Insurance Cost: " + insuranceCost);
            logger.info("Total with Insurance: " + totalWithInsurance);

            model.addAttribute("car", car);
            model.addAttribute("user", user);
            model.addAttribute("dateDebut", formattedDateDebut);
            model.addAttribute("dateFin", formattedDateFin);
            model.addAttribute("nbJours", duration);
            model.addAttribute("carPhotoUrl", carPhotoUrl);
            model.addAttribute("reservationId", reservation.getId());
            model.addAttribute("totalAmount", totalAmount);
            model.addAttribute("insuranceCost", insuranceCost);
            model.addAttribute("totalWithInsurance", totalWithInsurance);
            model.addAttribute("selectedInsurance", insurance);
            model.addAttribute("promotion", promotion);
            model.addAttribute("discountAmount", discountAmount);
            model.addAttribute("promo1", promo1);
            model.addAttribute("promo2", promo2);
            model.addAttribute("totalAmountAfterDiscount", totalAmountAfterDiscount);

            return "car/reservation";
        } catch (DateTimeParseException e) {
            return "redirect:/error";
        }
    }

    @GetMapping("/reservation/payment/{id}")
    public String getReservationPaymentPage(@PathVariable Long id,
                                            @RequestParam String dateDebut,
                                            @RequestParam String dateFin,
                                            @AuthenticationPrincipal CustomUserDetail userDetail,
                                            Model model) {
        Logger logger = LoggerFactory.getLogger(ReservationController.class);

        Reservation reservation = reservationService.getReservationById(id);
        if (reservation == null || !"PAYMENT_PENDING".equals(reservation.getStatut())) {
            return "redirect:/error";
        }

        User user = userDetail.getUser();
        if (!reservation.getUser().getId().equals(user.getId())) {
            return "redirect:/error";
        }

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate startDate = LocalDate.parse(dateDebut, formatter);
            LocalDate endDate = LocalDate.parse(dateFin, formatter);

            DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
            String formattedDateDebut = startDate.format(outputFormatter);
            String formattedDateFin = endDate.format(outputFormatter);

            Car car = reservation.getCar();
            String carPhotoUrl;
            if (car.getPhotos() != null && !car.getPhotos().isEmpty()) {
                carPhotoUrl = "/uploads/" + car.getPhotos().get(0).getUrl();
            } else {
                carPhotoUrl = "/images/carDefault.jpg";
            }

            double basePrice = car.getPrice().getMiddlePrice();
            long duration = ChronoUnit.DAYS.between(startDate, endDate);

            double totalAmount = basePrice * duration;
            Price carPrice = car.getPrice();
            double promo1 = carPrice.getPromo1();
            double promo2 = carPrice.getPromo2();
            double promotion = duration >= 30 ? promo2 : duration > 14 ? promo1 : 0;
            double discountAmount = totalAmount * (promotion / 100);
            double totalAmountAfterDiscount = totalAmount - discountAmount;
            double insuranceCost = 0;
            String insurance = reservation.getInsurance();
            switch (insurance) {
                case "mini-omnium":
                    insuranceCost = 2 * duration;
                    break;
                case "omnium":
                    insuranceCost = 5 * duration;
                    break;
                case "basic":
                default:
                    insuranceCost = 0;
                    break;
            }
            double totalWithInsurance = totalAmountAfterDiscount + insuranceCost;

            model.addAttribute("car", car);
            model.addAttribute("user", user);
            model.addAttribute("dateDebut", formattedDateDebut);
            model.addAttribute("dateFin", formattedDateFin);
            model.addAttribute("nbJours", duration);
            model.addAttribute("carPhotoUrl", carPhotoUrl);
            model.addAttribute("reservationId", reservation.getId());
            model.addAttribute("totalAmount", totalAmount);
            model.addAttribute("insuranceCost", insuranceCost);
            model.addAttribute("totalWithInsurance", totalWithInsurance);
            model.addAttribute("selectedInsurance", insurance);
            model.addAttribute("promotion", promotion);
            model.addAttribute("discountAmount", discountAmount);
            model.addAttribute("promo1", promo1);
            model.addAttribute("promo2", promo2);
            model.addAttribute("totalAmountAfterDiscount", totalAmountAfterDiscount);

            return "car/reservation";
        } catch (DateTimeParseException e) {
            return "redirect:/error";
        }
    }

    @GetMapping("/checkout")
    public String getCheckout() {
        return "checkout";
    }
}
