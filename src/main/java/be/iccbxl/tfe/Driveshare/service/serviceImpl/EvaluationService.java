package be.iccbxl.tfe.Driveshare.service.serviceImpl;

import be.iccbxl.tfe.Driveshare.DTO.CarDTO;
import be.iccbxl.tfe.Driveshare.DTO.CarMapper;
import be.iccbxl.tfe.Driveshare.model.Car;
import be.iccbxl.tfe.Driveshare.model.Evaluation;
import be.iccbxl.tfe.Driveshare.repository.CarRepository;
import be.iccbxl.tfe.Driveshare.repository.EvaluationRepository;
import be.iccbxl.tfe.Driveshare.service.EvaluationServiceI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EvaluationService implements EvaluationServiceI {

    @Autowired
    private EvaluationRepository evaluationRepository;

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
                .map(CarMapper::toCarDTO)
                .collect(Collectors.toList());
        if (carDTOs.size() > 4) {
            return carDTOs.subList(0, 4);
        } else {
            return carDTOs;
        }
    }


    }

