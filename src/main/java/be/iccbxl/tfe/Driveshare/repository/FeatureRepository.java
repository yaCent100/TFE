package be.iccbxl.tfe.Driveshare.repository;

import be.iccbxl.tfe.Driveshare.model.Feature;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FeatureRepository extends CrudRepository<Feature, Long> {
}
