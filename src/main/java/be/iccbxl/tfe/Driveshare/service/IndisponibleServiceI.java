package be.iccbxl.tfe.Driveshare.service;

import be.iccbxl.tfe.Driveshare.DTO.IndisponibleDTO;
import be.iccbxl.tfe.Driveshare.model.Indisponible;

import java.time.LocalDate;
import java.util.List;

public interface IndisponibleServiceI {

    List<IndisponibleDTO> getUnavailableDatesForCar(Long carId);
    void removeUnavailableDate(Long carId, LocalDate startDate, LocalDate endDate);

    List<IndisponibleDTO> findByCarId(Long carId);
    void addUnavailableDate(IndisponibleDTO unavailableDateDTO);
}
