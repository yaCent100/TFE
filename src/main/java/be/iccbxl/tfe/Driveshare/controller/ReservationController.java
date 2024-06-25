package be.iccbxl.tfe.Driveshare.controller;

import be.iccbxl.tfe.Driveshare.model.Car;
import be.iccbxl.tfe.Driveshare.model.User;
import be.iccbxl.tfe.Driveshare.security.CustomUserDetail;
import be.iccbxl.tfe.Driveshare.service.serviceImpl.CarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class ReservationController {

    @Autowired
    private CarService carService;

    @GetMapping("/reservation/{id}")
    public String getReservationPage(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetail userDetail, Model model) {
        Car car = carService.getCarById(id);
        User user = userDetail.getUser();
        if (car == null) {
            // Gérer le cas où la voiture n'est pas trouvée
            return "redirect:/error";
        }
        model.addAttribute("car", car);
        model.addAttribute("user", user);
        return "car/reservation";
    }
}
