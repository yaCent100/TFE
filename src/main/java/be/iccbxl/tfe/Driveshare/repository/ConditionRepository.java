package be.iccbxl.tfe.Driveshare.repository;

import be.iccbxl.tfe.Driveshare.model.Condition;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConditionRepository extends CrudRepository<Condition, Long> {
}
