package be.iccbxl.tfe.Driveshare.service;


import be.iccbxl.tfe.Driveshare.model.Document;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public interface DocumentServiceI {

    List<Document> getByUserId(Long id);

    void save(Document versoDoc);
}
