package be.iccbxl.tfe.Driveshare.service.serviceImpl;

import be.iccbxl.tfe.Driveshare.model.Condition;
import be.iccbxl.tfe.Driveshare.repository.ConditionRepository;
import be.iccbxl.tfe.Driveshare.service.ConditionServiceI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ConditionService implements ConditionServiceI {

    @Autowired
    private ConditionRepository conditionRepository;

    @Override
    public List<Condition> getAllConditions() {
        List<Condition> conditions = new ArrayList<>();
        conditionRepository.findAll().forEach(conditions::add);
        return conditions;
    }

    @Override
    public Condition getConditionById(Long id) {
        Optional<Condition> optionalCondition = conditionRepository.findById(id);
        return optionalCondition.orElse(null);
    }

    @Override
    public Condition saveCondition(Condition condition) {
        return conditionRepository.save(condition);
    }

    @Override
    public Condition updateCondition(Long id, Condition newCondition) {
        Optional<Condition> optionalCondition = conditionRepository.findById(id);
        if (optionalCondition.isPresent()) {
            Condition existingCondition = optionalCondition.get();
            existingCondition.setCondition(newCondition.getCondition()); // Assuming you have a setter for the condition field
            return conditionRepository.save(existingCondition);
        }
        return null;
    }

    @Override
    public void deleteCondition(Long id) {
        conditionRepository.deleteById(id);
    }
}
