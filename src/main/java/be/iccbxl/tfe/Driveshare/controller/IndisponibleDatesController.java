package be.iccbxl.tfe.Driveshare.controller;

import be.iccbxl.tfe.Driveshare.DTO.IndisponibleDTO;
import be.iccbxl.tfe.Driveshare.service.serviceImpl.CarService;
import be.iccbxl.tfe.Driveshare.service.serviceImpl.IndisponibleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
public class IndisponibleDatesController {

    @Autowired
    private IndisponibleService indisponibleService;
    @Autowired
    private CarService carService;


    @PostMapping("/save-availability")
    public String saveAvailability(
            @RequestParam Long carId,
            @RequestParam(required = false) List<String> unavailableStartDates,
            @RequestParam(required = false) List<String> unavailableEndDates,
            @RequestParam(required = false) List<String> availableStartDates,
            @RequestParam(required = false) List<String> availableEndDates,
            RedirectAttributes redirectAttributes) {

        // Afficher les paramètres reçus
        System.out.println("Car ID: " + carId);

        if (unavailableStartDates != null && unavailableEndDates != null) {
            System.out.println("Unavailable Start Dates: " + unavailableStartDates);
            System.out.println("Unavailable End Dates: " + unavailableEndDates);
        } else {
            System.out.println("No unavailable dates received");
        }

        if (availableStartDates != null && availableEndDates != null) {
            System.out.println("Available Start Dates: " + availableStartDates);
            System.out.println("Available End Dates: " + availableEndDates);
        } else {
            System.out.println("No available dates received");
        }

        // Gérer l'ajout d'indisponibilités
        if (unavailableStartDates != null && unavailableEndDates != null) {
            for (int i = 0; i < unavailableStartDates.size(); i++) {
                LocalDate start = LocalDate.parse(unavailableStartDates.get(i));
                LocalDate end = LocalDate.parse(unavailableEndDates.get(i));

                IndisponibleDTO indisponibleDTO = new IndisponibleDTO();
                indisponibleDTO.setStartDate(start);
                indisponibleDTO.setEndDate(end);
                indisponibleDTO.setVoitureId(carId);
                indisponibleService.addUnavailableDate(indisponibleDTO);
            }
        }

        // Gérer la suppression d'indisponibilités (disponibilités)
        if (availableStartDates != null && availableEndDates != null) {
            for (int i = 0; i < availableStartDates.size(); i++) {
                LocalDate start = LocalDate.parse(availableStartDates.get(i));
                LocalDate end = LocalDate.parse(availableEndDates.get(i));

                indisponibleService.removeUnavailableDate(carId, start, end);
            }
        }

        // Ajouter un message de succès à afficher après la redirection
        redirectAttributes.addFlashAttribute("successMessage", "Les disponibilités ont été mises à jour avec succès.");

        return "redirect:/account/cars";
    }



}
