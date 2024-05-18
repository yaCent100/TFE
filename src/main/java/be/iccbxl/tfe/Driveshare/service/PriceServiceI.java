package be.iccbxl.tfe.Driveshare.service;

import be.iccbxl.tfe.Driveshare.model.Car;
import be.iccbxl.tfe.Driveshare.model.Category;

import java.time.LocalDate;

public interface PriceServiceI {

    double calculatePrice(Category cat, LocalDate date);
    boolean isHighSeason(LocalDate date);
    boolean isLowSeason(LocalDate date);
}
