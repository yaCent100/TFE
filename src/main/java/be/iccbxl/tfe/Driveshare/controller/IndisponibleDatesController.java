package be.iccbxl.tfe.Driveshare.controller;

import be.iccbxl.tfe.Driveshare.DTO.IndisponibleDTO;
import be.iccbxl.tfe.Driveshare.model.Car;
import be.iccbxl.tfe.Driveshare.model.Indisponible;
import be.iccbxl.tfe.Driveshare.service.serviceImpl.CarService;
import be.iccbxl.tfe.Driveshare.service.serviceImpl.IndisponibleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

@Controller
public class IndisponibleDatesController {

    @Autowired
    private IndisponibleService indisponibleService;
    @Autowired
    private CarService carService;

    @PostMapping("/save-availability")
    public String saveAvailability(@RequestParam Long carId,
                                   @RequestParam List<String> startDates,
                                   @RequestParam List<String> endDates,
                                   @RequestParam List<String> statuses) {

        for (int i = 0; i < startDates.size(); i++) {
            LocalDate start = LocalDate.parse(startDates.get(i));
            LocalDate end = LocalDate.parse(endDates.get(i));
            String status = statuses.get(i);

            if ("unavailable".equals(status)) {
                IndisponibleDTO indisponibleDTO = new IndisponibleDTO();
                indisponibleDTO.setStartDate(start);
                indisponibleDTO.setEndDate(end);
                indisponibleDTO.setVoitureId(carId);
                indisponibleService.addUnavailableDate(indisponibleDTO);
            } else if ("available".equals(status)) {
                indisponibleService.removeUnavailableDate(carId, start, end);
            }
        }
        return "redirect:/account/cars";
    }
}
