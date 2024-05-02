package be.iccbxl.tfe.Driveshare.service.serviceImpl;

import be.iccbxl.tfe.Driveshare.model.Car;
import be.iccbxl.tfe.Driveshare.model.Evaluation;
import be.iccbxl.tfe.Driveshare.model.Reservation;
import be.iccbxl.tfe.Driveshare.repository.CarRepository;
import be.iccbxl.tfe.Driveshare.repository.ReservationRepository;
import be.iccbxl.tfe.Driveshare.service.CarServiceI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CarService implements CarServiceI {

    @Autowired
    private CarRepository carRepository;

    @Autowired
    private ReservationRepository reservationRepository;


    @Override
    public List<Car> getAllCars() {
        List<Car> cars = new ArrayList<>();
        carRepository.findAll().forEach(cars::add);
        return cars;
    }

    @Override
    public Car getCarById(Long id) {
        Optional<Car> optionalCar = carRepository.findById(id);
        return optionalCar.orElse(null);
    }

    @Override
    public Car addUser(Car car) {
        return null;
    }

    @Override
    public Car saveCar(Car car) {
        return carRepository.save(car);
    }

    @Override
    public Car updateCar(Long id, Car car) {
        Optional<Car> optionalCar = carRepository.findById(id);
        if (optionalCar.isPresent()) {
            Car existingCar = optionalCar.get();
            existingCar.setBrand(car.getBrand());
            existingCar.setModel(car.getModel());
            existingCar.setYear(car.getYear());
            // Mettre à jour d'autres attributs si nécessaire
            return carRepository.save(existingCar);
        } else {
            return null;
        }
    }

    @Override
    public void deleteCar(Long id) {
        carRepository.deleteById(id);
    }

    @Override
    public double calculateAverageRating(Car car) {
        List<Evaluation> evaluations = car.getEvaluations();
        if (evaluations.isEmpty()) {
            return 0.0;
        }

        double sum = 0.0;
        for (Evaluation evaluation : evaluations) {
            sum += evaluation.getNote();
        }

        return Math.round((sum / evaluations.size()) * 2) / 2.0;    }


    @Override
    public Map<Long, Double> getAverageRatingsForCars() {
        Iterable<Car> cars = carRepository.findAll();
        Map<Long, Double> averageRatings = new HashMap<>();

        for (Car car : cars) {
            double averageRating = calculateAverageRating(car);
            averageRatings.put(car.getId(), averageRating);
        }

        return averageRatings;
    }

    @Override
    public Map<Long, Integer> getReviewCountsForCars() {
        Iterable<Car> cars = carRepository.findAll();
        Map<Long, Integer> reviewCounts = new HashMap<>();

        for (Car car : cars) {
            int count = car.getEvaluations().size();
            reviewCounts.put(car.getId(), count);
        }

        return reviewCounts;
    }

    @Override
    public List<Car> searchAvailableCars(String address, LocalDate dateDebut, LocalDate dateFin) {
        // Récupérer toutes les voitures
        List<Car> allCars = (List<Car>) carRepository.findAll();

        // Filtrer les voitures disponibles pour l'adresse spécifiée
        List<Car> availableCars = allCars.stream()
                .filter(car -> car.getAdresse().equalsIgnoreCase(address))
                .collect(Collectors.toList());

        // Filtrer les voitures disponibles pour la période de temps spécifiée
        availableCars = availableCars.stream()
                .filter(car -> isCarAvailableForDates(car, dateDebut, dateFin))
                .collect(Collectors.toList());

        return availableCars;
    }


    private boolean isCarAvailableForDates(Car car, LocalDate dateDebut, LocalDate dateFin) {
        // Récupérer toutes les réservations de la voiture
        List<Reservation> reservations = car.getReservations();

        // Vérifier si la voiture n'a aucune réservation pour la période spécifiée
        for (Reservation reservation : reservations) {
            LocalDate reservationStartDate = reservation.getDebutLocation();
            LocalDate reservationEndDate = reservation.getFinLocation();

            // Vérifier s'il y a un chevauchement entre les dates de réservation et les dates spécifiées
            if ((dateDebut.isAfter(reservationStartDate) || dateDebut.isEqual(reservationStartDate)) &&
                    (dateDebut.isBefore(reservationEndDate) || dateDebut.isEqual(reservationEndDate)) ||
                    (dateFin.isAfter(reservationStartDate) || dateFin.isEqual(reservationStartDate)) &&
                            (dateFin.isBefore(reservationEndDate) || dateFin.isEqual(reservationEndDate))) {
                // Il y a un chevauchement de dates, la voiture n'est pas disponible
                return false;
            }
        }

        // Aucun chevauchement trouvé, la voiture est disponible
        return true;
    }








}






