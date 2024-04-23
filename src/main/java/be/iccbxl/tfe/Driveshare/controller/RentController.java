package be.iccbxl.tfe.Driveshare.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class RentController {

    @GetMapping("/whyRent")
    public String whyRentYourCar(){
        return "car/whyRent";
    }

    @GetMapping("/rent")
    public String rentYourCar() {


        return "car/rent";
    }

}
