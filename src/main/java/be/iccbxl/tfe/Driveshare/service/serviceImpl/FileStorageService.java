package be.iccbxl.tfe.Driveshare.service.serviceImpl;

import be.iccbxl.tfe.Driveshare.service.FileStorageServiceI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class FileStorageService implements FileStorageServiceI {
    @Value("${file.upload-dir.photo}")
    private String photoDir;

    @Value("${file.upload-dir.licence}")
    private String licenceDir;

    @Value("${file.upload-dir.identity}")
    private String identityDir;
    @Value("${file.upload-dir.registrationCard}")
    private String registrationCardDir;

    @Override
    public String storeFile(MultipartFile file, String directory, Long carId) {
        String uploadDir;
        switch (directory) {
            case "photo":
                uploadDir = Paths.get(photoDir, "car" + carId).toString();
                break;
            case "licence":
                uploadDir = Paths.get(licenceDir, "car" + carId).toString();
                break;
            case "identity":
                uploadDir = Paths.get(identityDir, "car" + carId).toString();
                break;
            case "registration":
                uploadDir = Paths.get(registrationCardDir, "car" + carId).toString();
                break;
            default:
                throw new IllegalArgumentException("Invalid directory: " + directory);
        }

        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            String fileName = file.getOriginalFilename();
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            return filePath.toString();
        } catch (IOException ex) {
            throw new RuntimeException("Failed to store file " + file.getOriginalFilename(), ex);
        }
    }

}
