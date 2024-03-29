package be.iccbxl.tfe.Driveshare.service;

import be.iccbxl.tfe.Driveshare.model.Car;
import be.iccbxl.tfe.Driveshare.model.Condition;

import java.util.List;

public interface ConditionServiceI {

    List<Condition> getAllConditions();
    Condition getConditionById(Long id);
    Condition saveCondition(Condition cond);
    Condition updateCondition(Long id, Condition cond);
    void deleteCondition(Long id);
}
