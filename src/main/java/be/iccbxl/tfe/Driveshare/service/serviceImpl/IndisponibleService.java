package be.iccbxl.tfe.Driveshare.service.serviceImpl;

import be.iccbxl.tfe.Driveshare.DTO.IndisponibleDTO;
import be.iccbxl.tfe.Driveshare.model.Car;
import be.iccbxl.tfe.Driveshare.model.Indisponible;
import be.iccbxl.tfe.Driveshare.model.Reservation;
import be.iccbxl.tfe.Driveshare.repository.CarRepository;
import be.iccbxl.tfe.Driveshare.repository.IndisponibleRepository;
import be.iccbxl.tfe.Driveshare.repository.ReservationRepository;
import be.iccbxl.tfe.Driveshare.service.IndisponibleServiceI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class IndisponibleService implements IndisponibleServiceI {

    @Autowired
    private IndisponibleRepository indisponibleRepository;

    @Autowired
    private CarRepository carRepository;

    @Autowired
    private ReservationRepository reservationRepository;



    public List<IndisponibleDTO> findByCarId(Long carId) {
        List<Indisponible> indisponibles = indisponibleRepository.findByCarId(carId);
        List<Reservation> reservations = reservationRepository.findByCarIdAndStatus(carId);

        // Filtrer les indisponibilités qui chevauchent une réservation avec statut "confirmed", "finished" ou "now"
        List<Indisponible> filteredIndisponibles = indisponibles.stream()
                .filter(indispo -> reservations.stream()
                        .noneMatch(reservation -> datesOverlap(
                                reservation.getStartLocation(), reservation.getEndLocation(),
                                indispo.getStartDate(), indispo.getEndDate())))
                .toList();

        return filteredIndisponibles.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private boolean datesOverlap(LocalDate start1, LocalDate end1, LocalDate start2, LocalDate end2) {
        return (start1.isBefore(end2) && end1.isAfter(start2));
    }

    @Override
    public List<IndisponibleDTO> getUnavailableDatesForCar(Long carId) {
        List<Indisponible> indisponibles = indisponibleRepository.findByCarId(carId);
        return indisponibles.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    @Override
    public void addUnavailableDate(IndisponibleDTO unavailableDateDTO) {
        Indisponible indisponible = new Indisponible();
        indisponible.setStartDate(unavailableDateDTO.getStartDate());
        indisponible.setEndDate(unavailableDateDTO.getEndDate());
        Car car = carRepository.findById(unavailableDateDTO.getVoitureId()).orElseThrow(() -> new RuntimeException("Voiture non trouvée"));
        indisponible.setCar(car);
        indisponibleRepository.save(indisponible);
    }


    @Override
    public void removeUnavailableDate(Long carId, LocalDate startDate, LocalDate endDate) {
        List<Indisponible> unavailableDates = indisponibleRepository.findByCarIdAndStartDateAndEndDate(carId, startDate, endDate);
        for (Indisponible date : unavailableDates) {
            indisponibleRepository.delete(date);
        }
    }

    private IndisponibleDTO convertToDTO(Indisponible indisponible) {
        IndisponibleDTO dto = new IndisponibleDTO();
        dto.setId(indisponible.getId());
        dto.setStartDate(indisponible.getStartDate());
        dto.setEndDate(indisponible.getEndDate());
        dto.setVoitureId(indisponible.getCar().getId());
        return dto;
    }

}
