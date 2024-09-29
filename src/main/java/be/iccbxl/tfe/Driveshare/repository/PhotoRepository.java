package be.iccbxl.tfe.Driveshare.repository;

import be.iccbxl.tfe.Driveshare.model.Photo;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PhotoRepository extends CrudRepository<Photo, Long> {


    @Modifying
    @Query(value = "DELETE FROM pictures WHERE car_id = :carId AND url = :photoUrl", nativeQuery = true)
    void deletePhotoByCarIdAndUrl(@Param("carId") Long carId, @Param("photoUrl") String photoUrl);
}

