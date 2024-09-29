package be.iccbxl.tfe.Driveshare.service.serviceImpl;

import be.iccbxl.tfe.Driveshare.DTO.DocumentDTO;
import be.iccbxl.tfe.Driveshare.DTO.MapperDTO;
import be.iccbxl.tfe.Driveshare.model.Car;
import be.iccbxl.tfe.Driveshare.model.Document;
import be.iccbxl.tfe.Driveshare.repository.DocumentRepository;
import be.iccbxl.tfe.Driveshare.service.DocumentServiceI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.print.Doc;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DocumentService implements DocumentServiceI {

    public final FileStorageService fileStorageService;

    public final DocumentRepository documentRepository;

    @Autowired
    public DocumentService(FileStorageService fileStorageService, DocumentRepository documentRepository) {
        this.fileStorageService = fileStorageService;
        this.documentRepository = documentRepository;
    }

    public List<DocumentDTO> getAllDocuments() {
        List<Document> documents = documentRepository.findAll();
        return documents.stream().map(MapperDTO::toDocumentDTO).collect(Collectors.toList());
    }




    @Override
    public List<Document> getByUserId(Long userId) {
        return documentRepository.findByUserId(userId);
    }

    @Override
    public List<Document> findAll() {
        return documentRepository.findAll();
    }

    @Override
    public void save(Document doc) {
        documentRepository.save(doc);
    }

    @Transactional
    public void deleteDocumentsByUserIdAndType(Long userId, String documentType) {
        try {
            System.out.println("Recherche des documents pour l'utilisateur avec l'ID: " + userId + " et le type: " + documentType);
            List<Document> documents = documentRepository.findByUserIdAndDocumentType(userId, documentType);
            if (!documents.isEmpty()) {
                for (Document document : documents) {
                    System.out.println("Document trouvé: " + document.getId());
                    documentRepository.delete(document);
                    System.out.println("Document supprimé avec l'ID: " + document.getId());
                }
                // Vérifier si les documents ont été supprimés
                documents = documentRepository.findByUserIdAndDocumentType(userId, documentType);
                if (documents.isEmpty()) {
                    System.out.println("Tous les documents ont été supprimés.");
                } else {
                    System.out.println("Certains documents n'ont pas été supprimés: " + documents);
                }
            } else {
                System.out.println("Aucun document trouvé pour l'utilisateur avec l'ID: " + userId + " et le type: " + documentType);
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la suppression des documents: " + e.getMessage());
            e.printStackTrace();
        }
    }


    public List<DocumentDTO> getByUserIdDocumentDTO(Long userId) {
        // Récupère les documents de l'utilisateur depuis le repository
        List<Document> documents = documentRepository.findByUserId(userId);

        // Mappe directement les entités vers des DTO
        return documents.stream()
                .map(MapperDTO::toDocumentDTO)  // Utilise le mapper pour chaque document
                .collect(Collectors.toList());
    }


    // Méthode pour vérifier si les documents recto et verso sont uploadés
    public boolean isUserIdentityUploaded(Long userId) {
        boolean rectoUploaded = documentRepository.existsByUserIdAndDocumentType(userId, "identity_recto");
        boolean versoUploaded = documentRepository.existsByUserIdAndDocumentType(userId, "identity_verso");

        // Retourne vrai uniquement si les deux documents sont uploadés
        return rectoUploaded && versoUploaded;
    }
}





