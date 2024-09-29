package be.iccbxl.tfe.Driveshare;

import be.iccbxl.tfe.Driveshare.service.serviceImpl.CarService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer {

    @Autowired
    private CarService carService;

   /*@EventListener(ApplicationReadyEvent.class)*/
    public void init() throws Exception {
        carService.updateCarCoordinates();
    }


}