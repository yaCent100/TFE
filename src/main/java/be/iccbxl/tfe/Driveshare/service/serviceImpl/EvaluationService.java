package be.iccbxl.tfe.Driveshare.service.serviceImpl;

import be.iccbxl.tfe.Driveshare.DTO.*;
import be.iccbxl.tfe.Driveshare.model.Car;
import be.iccbxl.tfe.Driveshare.model.Evaluation;
import be.iccbxl.tfe.Driveshare.model.Reservation;
import be.iccbxl.tfe.Driveshare.repository.CarRepository;
import be.iccbxl.tfe.Driveshare.repository.EvaluationRepository;
import be.iccbxl.tfe.Driveshare.repository.ReservationRepository;
import be.iccbxl.tfe.Driveshare.service.EvaluationServiceI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class EvaluationService implements EvaluationServiceI {

    @Autowired
    private EvaluationRepository evaluationRepository;
    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private CarRepository carRepository;

    @Autowired
    private CarService carService;

    @Autowired
    private PriceService priceService;

    @Override
    public List<Evaluation> getAllEvaluations() {
        List<Evaluation> evaluations = new ArrayList<>();
        evaluationRepository.findAll().forEach(evaluations::add);
        return evaluations;      }

    @Override
    public Evaluation getEvaluationById(Long id) {
        Optional<Evaluation> optionalEvaluation = evaluationRepository.findById(id);
        return optionalEvaluation.orElse(null);
    }

    @Override
    public Evaluation saveEvaluation(Evaluation evaluation) {
        return evaluationRepository.save(evaluation);
    }

    @Override
    public Evaluation updateEvaluation(Long id, Evaluation newEvaluation) {
        Optional<Evaluation> optionalEvaluation = evaluationRepository.findById(id);
        if (optionalEvaluation.isPresent()) {
            Evaluation existingEvaluation = optionalEvaluation.get();
            existingEvaluation.setNote(newEvaluation.getNote());
            existingEvaluation.setAvis(newEvaluation.getAvis());
            return evaluationRepository.save(existingEvaluation);
        }
        return null;
    }

    @Override
    public void deleteEvaluation(Long id) {
        evaluationRepository.deleteById(id);
    }


    public List<CarDTOHome> getTop4CarsWithFiveStarRating() {
        List<Car> cars = evaluationRepository.findTop4CarsWithFiveStarRating();
        Map<Long, Double> averageRatings = carService.getAverageRatingsForCars(); // Map des moyennes

        // Utilisation du PriceService pour calculer le prix affiché
        LocalDate currentDate = LocalDate.now();  // Vous pouvez ajuster cela en fonction de la date souhaitée

        return cars.stream()
                .map(car -> {
                    double displayPrice = priceService.calculateDisplayPrice(car.getPrice(), currentDate);  // Calcul du displayPrice
                    return new CarDTOHome(
                            car.getId(),                     // id de la voiture
                            car.getBrand(),                  // marque
                            car.getModel(),                 // modèle
                            car.getLocality(),
                            car.getPhotos().get(0).getUrl(),
                            displayPrice,                    // prix affiché calculé
                            averageRatings.getOrDefault(car.getId(), 0.0)  // moyenne des évaluations
                    );
                })
                .collect(Collectors.toList());
    }





    public EvaluationDTO createEvaluation(EvaluationDTO evaluationDTO) {
        // Vérifier que la réservation existe
        Reservation reservation = reservationRepository.findById(evaluationDTO.getReservationId())
                .orElseThrow(() -> new IllegalArgumentException("Réservation non trouvée"));

        // Créer l'entité Evaluation
        Evaluation evaluation = new Evaluation();
        evaluation.setNote(evaluationDTO.getNote());
        evaluation.setAvis(evaluationDTO.getAvis());
        evaluation.setCreatedAt(LocalDateTime.now());
        evaluation.setReservation(reservation);

        // Sauvegarder l'évaluation
        Evaluation savedEvaluation = evaluationRepository.save(evaluation);

        // Retourner l'objet DTO avec l'ID généré
        evaluationDTO.setId(savedEvaluation.getId());
        return evaluationDTO;
    }

    public boolean evaluationExists(Long reservationId) {
        return evaluationRepository.existsByReservationId(reservationId);
    }

    public List<EvaluationDTO> getAllEvaluationsGroupedByCar() {
        List<Evaluation> evaluations = evaluationRepository.findAllGroupedByCar();
        return evaluations.stream()
                .map(MapperDTO::toEvaluationDTO)
                .collect(Collectors.toList());
    }


    public void deleteEvaluationById(Long id) {
        if (!evaluationRepository.existsById(id)) {
            throw new IllegalArgumentException("L'évaluation avec l'ID " + id + " n'existe pas.");
        }
        evaluationRepository.deleteById(id);
    }


    // Méthode pour obtenir les données du tableau de bord

    public EvaluationDashboardDTO getEvaluationDashboardData() {
        List<Evaluation> evaluations = (List<Evaluation>) evaluationRepository.findAll();
        List<Reservation> reservations = reservationRepository.findAll();

        int totalEvaluations = evaluations.size();
        double averageRating = evaluations.stream()
                .mapToDouble(Evaluation::getNote)
                .average()
                .orElse(0.0);

        // Calcul du pourcentage d'évaluations par rapport aux réservations avec statut FINISHED
        List<Reservation> finishedReservations = reservations.stream()
                .filter(reservation -> reservation.getStatut().equals("FINISHED")) // Filtre sur le statut "FINISHED"
                .collect(Collectors.toList());

        int totalFinishedReservations = finishedReservations.size(); // Nombre total de réservations terminées
        double evaluationReservationPercentage = totalFinishedReservations > 0 ? (double) totalEvaluations / totalFinishedReservations * 100 : 0;


        // Calcul du pourcentage d'évaluations à 5 étoiles
        long fiveStarEvaluations = evaluations.stream().filter(e -> e.getNote() == 5).count();
        double evaluationFiveStarsPercentage = totalEvaluations > 0 ? (double) fiveStarEvaluations / totalEvaluations * 100 : 0;

        // Utilisation d'un Set pour éviter les doublons d'utilisateurs
        Set<Long> uniqueUsers = new HashSet<>();
        Map<String, Long> evaluationsByDay = new HashMap<>();

        for (Evaluation evaluation : evaluations) {
            if (evaluation.getReservation() != null && evaluation.getReservation().getUser() != null) {
                uniqueUsers.add(evaluation.getReservation().getUser().getId());

                // Calcul des évaluations par jour
                String day = evaluation.getCreatedAt().toLocalDate().toString();
                evaluationsByDay.put(day, evaluationsByDay.getOrDefault(day, 0L) + 1);
            }
        }

        int totalUsers = uniqueUsers.size();

        return new EvaluationDashboardDTO(totalEvaluations, averageRating, totalUsers, totalFinishedReservations,
                evaluationReservationPercentage, evaluationFiveStarsPercentage, evaluationsByDay);
    }






    public List<EvaluationDTO> getEvaluationsByCarId(Long carId) {
        List<Evaluation> evaluations = evaluationRepository.findByCarId(carId);
        return evaluations.stream().map(MapperDTO::toEvaluationDTO).collect(Collectors.toList());
    }
}

