package be.iccbxl.tfe.Driveshare.restController.admin;

import be.iccbxl.tfe.Driveshare.service.serviceImpl.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.nio.file.Paths;

@RestController
@RequestMapping("/api/files")
public class AdminFileDownloadRestController {
    @Autowired
    private FileStorageService fileStorageService;


    @GetMapping("/{directory}/{filename:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String directory, @PathVariable String filename) {
        String filePath = directory + "/" + filename;
        Resource resource = fileStorageService.loadFileAsResource(filePath);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
}
