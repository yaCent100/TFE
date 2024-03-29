package be.iccbxl.tfe.Driveshare.service;

import be.iccbxl.tfe.Driveshare.model.Equipment;
import be.iccbxl.tfe.Driveshare.model.Evaluation;

import java.util.List;

public interface EvaluationServiceI {

    List<Evaluation> getAllEvaluations();
    Evaluation getEvaluationById(Long id);
    Evaluation saveEvaluation(Evaluation evaluation);
    Evaluation updateEvaluation(Long id, Evaluation evaluation);
    void deleteEvaluation(Long id);
}
