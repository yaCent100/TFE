package be.iccbxl.tfe.Driveshare.repository;

import be.iccbxl.tfe.Driveshare.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {

    List<Document> findByUserId(Long userId);

    @Query("SELECT d FROM Document d WHERE d.user.id = :userId AND d.documentType = :documentType")
    List<Document> findByUserIdAndDocumentType(@Param("userId") Long userId, @Param("documentType") String documentType);

    boolean existsByUserIdAndDocumentType(Long userId, String identityRecto);
}
