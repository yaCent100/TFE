package be.iccbxl.tfe.Driveshare.repository;

import be.iccbxl.tfe.Driveshare.model.Category;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends CrudRepository<Category, Long> {
}
