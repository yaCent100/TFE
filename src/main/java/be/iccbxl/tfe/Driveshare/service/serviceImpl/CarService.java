package be.iccbxl.tfe.Driveshare.service.serviceImpl;

import be.iccbxl.tfe.Driveshare.model.Car;
import be.iccbxl.tfe.Driveshare.model.CarRental;
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
        List<CarRental> carRentals = car.getCarRentals();
        if (carRentals.isEmpty()) {
            return 0.0; // Retourne 0 si aucune location n'est associée à la voiture
        }

        double sum = 0.0;
        int totalEvaluations = 0; // Compte le nombre total d'évaluations pour faire la moyenne

        // Parcourir chaque location de la voiture
        for (CarRental carRental : carRentals) {
            List<Evaluation> evaluations = carRental.getEvaluations(); // Obtenir les évaluations de chaque location
            for (Evaluation evaluation : evaluations) {
                sum += evaluation.getNote(); // Ajouter la note de l'évaluation au total
                totalEvaluations++; // Incrémenter le compteur d'évaluations
            }
        }

        if (totalEvaluations == 0) {
            return 0.0; // Évite la division par zéro si aucune évaluation n'est présente
        }

        // Calculer la moyenne et arrondir à la demi-note la plus proche
        return Math.round((sum / totalEvaluations) * 2) / 2.0;
    }


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
            List<CarRental> carRentals = car.getCarRentals(); // Obtenir toutes les locations de la voiture
            int count = 0;

            for (CarRental carRental : carRentals) {
                count += carRental.getEvaluations().size(); // Ajouter le nombre d'évaluations pour chaque location
            }

            reviewCounts.put(car.getId(), count); // Stocker le nombre total d'évaluations pour cette voiture
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


    public boolean isCarAvailableForDates(Car car, LocalDate dateDebut, LocalDate dateFin) {
        // Récupérer toutes les réservations de la voiture
        List<CarRental> carRentals = car.getCarRentals();

        // Parcourir chaque location de voiture (CarRental) pour vérifier les dates de réservation
        for (CarRental carRental : carRentals) {
            List<Reservation> reservations = carRental.getReservations();

            // Vérifier chaque réservation pour un chevauchement avec les dates spécifiées
            for (Reservation reservation : reservations) {
                LocalDate reservationStartDate = reservation.getDebutLocation();
                LocalDate reservationEndDate = reservation.getFinLocation();

                // Vérifier s'il y a un chevauchement entre les dates de réservation et les dates spécifiées
                if ((dateDebut.isEqual(reservationStartDate) || dateDebut.isAfter(reservationStartDate)) &&
                        (dateDebut.isEqual(reservationEndDate) || dateDebut.isBefore(reservationEndDate)) ||
                        (dateFin.isEqual(reservationStartDate) || dateFin.isAfter(reservationStartDate)) &&
                                (dateFin.isEqual(reservationEndDate) || dateFin.isBefore(reservationEndDate))) {
                    // Il y a un chevauchement de dates, la voiture n'est pas disponible
                    return false;
                }
            }
        }

        // Aucun chevauchement trouvé, la voiture est disponible
        return true;
    }









}






