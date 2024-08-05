package be.iccbxl.tfe.Driveshare.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminReservationController {

    @GetMapping("/admin/reservations")
    public String adminInterface(Model model){
        return "admin/reservation/index";
    }
}
