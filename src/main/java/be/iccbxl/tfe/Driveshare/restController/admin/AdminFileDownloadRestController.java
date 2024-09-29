package be.iccbxl.tfe.Driveshare.restController.admin;

import be.iccbxl.tfe.Driveshare.service.serviceImpl.FileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.nio.file.Paths;

@RestController
@Tag(name = "Admin File Management", description = "API pour la gestion et le téléchargement des fichiers")
public class AdminFileDownloadRestController {

    @Autowired
    private FileStorageService fileStorageService;

    @GetMapping("/api/files/{directory}/{fileName}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String directory, @PathVariable String fileName) {
        Resource resource = fileStorageService.loadFileAsResource(directory + "/" + fileName);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .body(resource);
    }



}

