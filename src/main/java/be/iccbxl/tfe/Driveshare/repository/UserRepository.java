package be.iccbxl.tfe.Driveshare.repository;

import be.iccbxl.tfe.Driveshare.model.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends CrudRepository<User, Long> {
    User findByEmail(String email);

    long countCarsById(Long userId);



    long countByCreatedAtAfter(LocalDate date);

    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt BETWEEN :startDate AND :endDate GROUP BY MONTH(u.createdAt)")
    List<Integer> getUserGrowthPerMonth();

    List<User> findTop10ByOrderByCreatedAtDesc();

}
