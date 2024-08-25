package be.iccbxl.tfe.Driveshare.service.serviceImpl;

import be.iccbxl.tfe.Driveshare.DTO.CarDTO;
import be.iccbxl.tfe.Driveshare.DTO.EvaluationDTO;
import be.iccbxl.tfe.Driveshare.DTO.EvaluationDashboardDTO;
import be.iccbxl.tfe.Driveshare.DTO.MapperDTO;
import be.iccbxl.tfe.Driveshare.model.Car;
import be.iccbxl.tfe.Driveshare.model.Evaluation;
import be.iccbxl.tfe.Driveshare.model.Reservation;
import be.iccbxl.tfe.Driveshare.repository.CarRepository;
import be.iccbxl.tfe.Driveshare.repository.EvaluationRepository;
import be.iccbxl.tfe.Driveshare.repository.ReservationRepository;
import be.iccbxl.tfe.Driveshare.service.EvaluationServiceI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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


    public List<CarDTO> getTop4CarsWithFiveStarRating() {
        List<Car> cars = evaluationRepository.findTop4CarsWithFiveStarRating();
        List<CarDTO> carDTOs = cars.stream()
                .map(MapperDTO::toCarDTO)
                .collect(Collectors.toList());
        if (carDTOs.size() > 4) {
            return carDTOs.subList(0, 4);
        } else {
            return carDTOs;
        }
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

        int totalEvaluations = evaluations.size();
        double averageRating = evaluations.stream()
                .mapToDouble(Evaluation::getNote)
                .average()
                .orElse(0.0);

        // Utilisation d'un Set pour éviter les doublons d'utilisateurs
        Set<Long> uniqueUsers = new HashSet<>();
        Map<String, Long> evaluationsByDay = new HashMap<>();

        // Traitement de chaque évaluation
        for (Evaluation evaluation : evaluations) {
            try {
                // Vérifier si la réservation existe et est valide
                if (evaluation.getReservation() != null && evaluation.getReservation().getUser() != null) {
                    // Ajout de l'utilisateur unique
                    uniqueUsers.add(evaluation.getReservation().getUser().getId());

                    // Calcul des évaluations par jour
                    String day = evaluation.getCreatedAt().toLocalDate().toString();
                    evaluationsByDay.put(day, evaluationsByDay.getOrDefault(day, 0L) + 1);
                } else {
                    // Vous pouvez loguer ou ignorer cette évaluation si la réservation ou l'utilisateur est manquant
                    System.out.println("Évaluation sans réservation valide ou utilisateur, ID: " + evaluation.getId());
                }
            } catch (Exception e) {
                // Loguer l'exception en cas de problème inattendu
                System.err.println("Erreur lors du traitement de l'évaluation ID: " + evaluation.getId() + " - " + e.getMessage());
            }
        }

        // Calcul du nombre total d'utilisateurs uniques
        int totalUsers = uniqueUsers.size();

        // Retourner un DTO avec les données du tableau de bord
        return new EvaluationDashboardDTO(totalEvaluations, averageRating, totalUsers, evaluationsByDay);
    }



    public List<EvaluationDTO> getEvaluationsByCarId(Long carId) {
        List<Evaluation> evaluations = evaluationRepository.findByCarId(carId);
        return evaluations.stream().map(MapperDTO::toEvaluationDTO).collect(Collectors.toList());
    }
}

