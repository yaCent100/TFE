package be.iccbxl.tfe.Driveshare.service.serviceImpl;

import be.iccbxl.tfe.Driveshare.DTO.CarDTO;
import be.iccbxl.tfe.Driveshare.DTO.EvaluationDTO;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
    }

