package be.iccbxl.tfe.Driveshare.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminController {

    @GetMapping("/admin")
    public String adminInterface(){
        return "admin/dashboard";
    }
}
