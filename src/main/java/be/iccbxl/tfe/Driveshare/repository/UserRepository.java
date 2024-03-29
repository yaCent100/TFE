package be.iccbxl.tfe.Driveshare.repository;

import be.iccbxl.tfe.Driveshare.model.User;
import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User, Long> {
}
