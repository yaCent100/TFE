package be.iccbxl.tfe.Driveshare.controller;

import be.iccbxl.tfe.Driveshare.model.Car;
import be.iccbxl.tfe.Driveshare.service.serviceImpl.CarService;
import be.iccbxl.tfe.Driveshare.service.serviceImpl.EvaluationService;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class CarController {


    private final CarService carService;
    private final EvaluationService evaluationService;

    @Autowired
    public CarController(CarService carService, EvaluationService evaluationService) {
        this.carService = carService;
        this.evaluationService = evaluationService;
    }
    @GetMapping("/cars")
    public String getAllCars(Model model) {
        List<Car> cars = carService.getAllCars();
        model.addAttribute("cars", cars);
        return "car/index";
    }




    @GetMapping("/cars/{id}")
    public String getCarById(@PathVariable Long id, Model model) {
        // Récupérer la voiture par son ID
        Car car = carService.getCarById(id);

        // Vérifier si la voiture existe
        if (car == null) {
            // Gérer le cas où la voiture n'est pas trouvée, par exemple en renvoyant une erreur 404
            return "error/404"; // à adapter selon votre gestion d'erreurs
        }

        // Calculer la note moyenne associée à cette voiture
        double averageRating = evaluationService.calculateAverageRating(car);

        // Ajouter la voiture et la note moyenne au modèle
        model.addAttribute("car", car);
        model.addAttribute("averageRating", averageRating);

        return "car/details"; // à adapter selon le nom de votre vue
    }
}
