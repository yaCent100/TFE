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
    @Value("${file.upload-dir.photoCar}")
    private String photoCarDir;

    @Value("${file.upload-dir.licence}")
    private String licenceDir;

    @Value("${file.upload-dir.identity}")
    private String identityDir;
    @Value("${file.upload-dir.registrationCard}")
    private String registrationCardDir;

    @Value("${file.upload-dir.icons}")
    private String iconsDir;



    @Override
    public String storeFile(MultipartFile file, String directory) {
        String uploadDir;
        switch (directory) {
            case "photo-car":
                uploadDir = photoCarDir;
                break;
            case "licence":
                uploadDir = licenceDir;
                break;
            case "identity":
                uploadDir = identityDir;
                break;
            case "registration":
                uploadDir = registrationCardDir;
                break;
            case "icons":
                uploadDir = iconsDir;
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
            return Paths.get(directory, fileName).toString();
        } catch (IOException ex) {
            throw new RuntimeException("Failed to store file " + file.getOriginalFilename(), ex);
        }
    }

    @Override
    public void deleteFile(String fileName) {
        try {
            Path filePath = Paths.get("src/main/resources/static").resolve(fileName).normalize();
            Files.deleteIfExists(filePath);
        } catch (IOException ex) {
            throw new RuntimeException("Could not delete file: " + fileName, ex);
        }
    }

}
