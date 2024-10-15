package be.iccbxl.tfe.Driveshare.controller;

import be.iccbxl.tfe.Driveshare.DTO.ReservationDTO;
import be.iccbxl.tfe.Driveshare.model.*;
import be.iccbxl.tfe.Driveshare.security.CustomUserDetail;
import be.iccbxl.tfe.Driveshare.service.serviceImpl.CarService;
import be.iccbxl.tfe.Driveshare.service.serviceImpl.DateService;
import be.iccbxl.tfe.Driveshare.service.serviceImpl.EmailService;
import be.iccbxl.tfe.Driveshare.service.serviceImpl.ReservationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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

    @Autowired
    private EmailService emailService;

    @Autowired
    private DateService dateService;

    @GetMapping("/reservation/{id}")
    public String getReservationPage(@PathVariable Long id,
                                     @RequestParam String dateDebut,
                                     @RequestParam String dateFin,
                                     @RequestParam(required = false, defaultValue = "basic") String insurance,
                                     @AuthenticationPrincipal CustomUserDetail userDetail,
                                     RedirectAttributes redirectAttributes, Model model) {
        Logger logger = LoggerFactory.getLogger(ReservationController.class);

        Car car = carService.getCarById(id);
        User user = userDetail.getUser();
        if (user == null || user.getId() == null) {
            logger.error("Invalid user details: " + user);
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid user session.");
            return "redirect:/error";
        }
        if (car == null) {
            return "redirect:/error";
        }

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate startDate = LocalDate.parse(dateDebut, formatter);
            LocalDate endDate = LocalDate.parse(dateFin, formatter);

            boolean isAvailable = carService.isCarAvailableForDates(car, startDate, endDate);

            if (!isAvailable) {
                redirectAttributes.addFlashAttribute("errorMessage", "La voiture n'est pas disponible pour les dates sélectionnées.");
                return "redirect:/cars/" + id;
            }

            long duration = ChronoUnit.DAYS.between(startDate, endDate);
            logger.info("Duration: " + duration);

            Reservation reservation = new Reservation();
            reservation.setCar(car);
            reservation.setUser(user);
            reservation.setStartLocation(startDate);
            reservation.setEndLocation(endDate);
            reservation.setNbJours((int) duration);
            reservation.setCreatedAt(LocalDateTime.now());
            reservation.setInsurance(insurance);

            if ("manuelle".equals(car.getModeReservation())) {
                reservation.setStatut("RESPONSE_PENDING");
                reservation.setCreatedAt(LocalDateTime.now());
                reservationService.saveReservation(reservation);

                // Envoi d'un email au propriétaire pour l'informer
                User carOwner = car.getUser();
                String ownerEmail = carOwner.getEmail();
                String subject = "Nouvelle demande de réservation à valider";
                String body = String.format("Bonjour %s,\n\nUne demande de réservation a été faite pour votre voiture %s (%s) du %s au %s.\n"
                                + "Vous avez 24 heures pour accepter ou refuser cette demande.\n\nMerci,\nDriveShare",
                        carOwner.getFirstName(),
                        car.getBrand(),
                        car.getModel(),
                        startDate,
                        endDate
                );
                emailService.sendNotificationEmail(ownerEmail, subject, body);

                redirectAttributes.addFlashAttribute("successMessage", "Votre demande a été envoyée au propriétaire. Vous recevrez une réponse sous 24h.");
                return "redirect:/account/reservations";
            } else {
                reservation.setStatut("PAYMENT_PENDING");
                reservation.setCreatedAt(LocalDateTime.now());
                reservationService.saveReservation(reservation);

                // Envoi d'un email à l'utilisateur pour le paiement
                String userEmail = user.getEmail();
                String subject = "Paiement en attente pour votre réservation";
                String body = String.format("Bonjour %s,\n\nVotre réservation pour la voiture %s (%s) du %s au %s est en attente de paiement.\n"
                                + "Vous avez 48 heures pour effectuer le paiement, sinon la réservation sera annulée.\n\nMerci,\nDriveShare",
                        user.getFirstName(),
                        car.getBrand(),
                        car.getModel(),
                        startDate,
                        endDate
                );
                emailService.sendNotificationEmail(userEmail, subject, body);

                // Logique de paiement et redirection
                DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
                String formattedDateDebut = startDate.format(outputFormatter);
                String formattedDateFin = endDate.format(outputFormatter);

                String carPhotoUrl;
                if (car.getPhotos() != null && !car.getPhotos().isEmpty()) {
                    carPhotoUrl = "/uploads/photo-car/" + car.getPhotos().get(0).getUrl();
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
            }
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
            // Utilisation de DateService pour parser les dates non standard
            LocalDate startDate = dateService.parseNonStandardDate(dateDebut);
            LocalDate endDate = dateService.parseNonStandardDate(dateFin);

            DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
            String formattedDateDebut = startDate.format(outputFormatter);
            String formattedDateFin = endDate.format(outputFormatter);

            Car car = reservation.getCar();
            String carPhotoUrl;
            if (car.getPhotos() != null && !car.getPhotos().isEmpty()) {
                carPhotoUrl = "/uploads/photo-car/" + car.getPhotos().get(0).getUrl();
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
        } catch (RuntimeException e) {
            logger.error("Erreur lors du parsing des dates", e);
            return "redirect:/error";
        }
    }

    @GetMapping("/checkout")
    public String getCheckout() {
        return "checkout";
    }
}
