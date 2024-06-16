package be.iccbxl.tfe.Driveshare.service.serviceImpl;

import be.iccbxl.tfe.Driveshare.model.Car;
import be.iccbxl.tfe.Driveshare.model.Document;
import be.iccbxl.tfe.Driveshare.repository.DocumentRepository;
import be.iccbxl.tfe.Driveshare.service.DocumentServiceI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.print.Doc;
import java.util.List;
import java.util.Optional;

@Service
public class DocumentService implements DocumentServiceI {

    public final FileStorageService fileStorageService;

    public final DocumentRepository documentRepository;

    @Autowired
    public DocumentService(FileStorageService fileStorageService, DocumentRepository documentRepository) {
        this.fileStorageService = fileStorageService;
        this.documentRepository = documentRepository;
    }


    @Override
    public List<Document> getByUserId(Long userId) {
        return documentRepository.findByUserId(userId);
    }

    @Override
    public void save(Document doc) {
        documentRepository.save(doc);
    }
}

