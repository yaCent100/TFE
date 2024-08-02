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
    public String showPendingCars(Model model) {
        List<Car> pendingCars = carService.getPendingCars();
        model.addAttribute("pendingCars", pendingCars);
        return "admin/cars/car-pending";
    }

    @GetMapping("/admin/cars/online")
    public String showApprovedCars(Model model) {
        List<Car> approvedCars = carService.getOnlineCars();
        model.addAttribute("approvedCars", approvedCars);
        return "admin/cars/car-online";
    }

    @GetMapping("/admin/cars/gestion")
    public String showGestionCars(Model model) {
        List<Category> categories = categoryService.getAllCategory();
        List<Equipment> equipments = equipmentService.getAllEquipments();
        List<Feature> features = featureService.getAllFeatures();

        model.addAttribute("categories", categories);
        model.addAttribute("equipments", equipments);
        model.addAttribute("features", features);

        return "admin/cars/car-gestion";
    }

    @PostMapping("/admin/cars/gestion/category")
    public String addCategory(@RequestParam("category") String categoryName) {
        // Assurez-vous que le nom de la catégorie est correctement traité
        Category category = new Category();
        category.setCategory(categoryName);
        categoryService.saveCategory(category);
        return "redirect:/admin/cars/gestion"; // Redirection après l'ajout
    }
    @PostMapping("admin/cars/gestion/equipment")
    public String addEquipment(@RequestParam("equipmentName") String equipmentName,
                               @RequestParam("equipmentIcon") MultipartFile file) {
        try {
            // Stocker le fichier et obtenir le chemin relatif
            String filePath = fileStorageService.storeFile(file, "icons");

            // Créer et sauvegarder l'équipement
            Equipment equipment = new Equipment();
            equipment.setEquipment(equipmentName);
            equipment.setIcone(filePath);
            equipmentService.saveEquipment(equipment);

        } catch (Exception e) {
            e.printStackTrace();
            // Gérer l'erreur de façon appropriée
        }
        return "redirect:/admin/cars/gestion"; // Redirection après l'ajout
    }

    @PostMapping("/admin/cars/gestion/feature")
    public String addFeature(@ModelAttribute Feature featureName, @RequestParam String featureDescription) {
        featureService.saveFeature(featureName, featureDescription);
        return "redirect:/admin/cars/car-gestion";
    }
}
