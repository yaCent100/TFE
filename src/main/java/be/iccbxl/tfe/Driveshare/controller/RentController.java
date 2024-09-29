package be.iccbxl.tfe.Driveshare.controller;

import be.iccbxl.tfe.Driveshare.DTO.CarDTO;
import be.iccbxl.tfe.Driveshare.model.*;
import be.iccbxl.tfe.Driveshare.security.CustomUserDetail;
import be.iccbxl.tfe.Driveshare.service.serviceImpl.*;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class RentController {

    private final DateService dateService;

    private final CategoryService categoryService;

    private final FeatureService featureService;

    private final CarService carService;

    private final EquipmentService equipmentService;

    private final DocumentService documentService;


    @Autowired
    public RentController(CategoryService categoryService, DateService dateService, FeatureService featureService,
                          CarService carService, EquipmentService equipmentService, DocumentService documentService) {
        this.dateService = dateService;
        this.categoryService = categoryService;
        this.featureService = featureService;
        this.carService = carService;
        this.equipmentService = equipmentService;
        this.documentService = documentService;
    }



    @GetMapping("/whyRent")
    public String whyRentYourCar(){
        return "car/whyRent";
    }


    @GetMapping("/rent")
    public String rentYourCar(Model model, @AuthenticationPrincipal CustomUserDetail userDetails) {

        if (userDetails != null) {
            User user = userDetails.getUser();
            model.addAttribute("user", user);

            // Vérifier si les documents d'identité (recto et verso) sont déjà uploadés
            boolean userIdentityUploaded = documentService.isUserIdentityUploaded(user.getId());
            model.addAttribute("userIdentityUploaded", userIdentityUploaded);
        }



        List<Feature> compteurFeatures = featureService.findByCategory("Compteur");
        List<Feature> placesFeatures = featureService.findByCategory("Places");
        List<Feature> boiteFeatures = featureService.findByCategory("Boite");
        List<Feature> portesFeatures = featureService.findByCategory("Portes");
        List<Equipment> equipments = equipmentService.getAllEquipments();


        List<Category> categories = categoryService.getAllCategory();

        CarDTO carDTO = new CarDTO();

        model.addAttribute("carDTO", carDTO);
        model.addAttribute("categories", categories);
        model.addAttribute("days", dateService.range(1, 31));
        model.addAttribute("months", dateService.getMonths());
        model.addAttribute("years", dateService.getYears(10));
        model.addAttribute("compteurFeatures", compteurFeatures);
        model.addAttribute("placesFeatures", placesFeatures);
        model.addAttribute("boiteFeatures", boiteFeatures);
        model.addAttribute("portesFeatures", portesFeatures);
        model.addAttribute("equipments", equipments);

        return "car/rent";
    }

    @PostMapping("/saveCar")
    public String submitCarForm(@ModelAttribute @Valid CarDTO carDTO, BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Validation failed!");
            bindingResult.getAllErrors().forEach(error -> {
                System.out.println(error.getDefaultMessage());
            });
            return "redirect:/rent";
        }

        try {

            carService.createCar(carDTO);

            redirectAttributes.addFlashAttribute("successMessage", "La voiture à été enregistré avec succées!");

            return "redirect:/account/cars";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error saving car: " + e.getMessage());
            return "redirect:/rent";
        }
    }

}
