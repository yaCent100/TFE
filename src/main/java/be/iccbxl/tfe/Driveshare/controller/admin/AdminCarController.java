package be.iccbxl.tfe.Driveshare.controller.admin;

import be.iccbxl.tfe.Driveshare.model.Car;
import be.iccbxl.tfe.Driveshare.model.Category;
import be.iccbxl.tfe.Driveshare.model.Equipment;
import be.iccbxl.tfe.Driveshare.model.Feature;
import be.iccbxl.tfe.Driveshare.service.serviceImpl.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
public class AdminCarController {

    @Autowired
    private CarService carService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private EquipmentService equipmentService;

    @Autowired
    private FeatureService featureService;

    @Autowired
    private FileStorageService fileStorageService;


    @GetMapping("/admin")
    public String adminInterface(){
        return "admin/dashboard";
    }


    @GetMapping("/admin/cars/pending")
    public String showPendingCars() {
        return "admin/cars/car-pending";
    }

    @GetMapping("/admin/cars/online")
    public String showApprovedCars() {
        return "admin/cars/car-online";
    }

    @GetMapping("/admin/cars/gestion")
    public String showGestionCars() {
        return "admin/cars/car-gestion";
    }

    @GetMapping("/admin/cars/details")
    public String showCarDetail(@RequestParam Long id) {
        return "admin/cars/car-detail";
    }



}
