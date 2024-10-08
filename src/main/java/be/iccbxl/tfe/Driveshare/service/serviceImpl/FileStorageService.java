package be.iccbxl.tfe.Driveshare.service.serviceImpl;

import be.iccbxl.tfe.Driveshare.service.FileStorageServiceI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
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

    @Value("${file.upload-dir.identityCard}")
    private String identityDir;

    @Value("${file.upload-dir.registrationCard}")
    private String registrationCardDir;

    @Value("${file.upload-dir.icons}")
    private String iconsDir;

    @Value("${file.upload-dir.profil}")
    private String profilDir;

    @Value("${file.upload-dir}")
    private String baseUploadDir;

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
            case "identityCard":
                uploadDir = identityDir;
                break;
            case "registrationCard":
                uploadDir = registrationCardDir;
                break;
            case "icons":
                uploadDir = iconsDir;
                break;
            case "profil":
                uploadDir = profilDir;
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
            if (fileName == null) {
                throw new RuntimeException("File name is invalid");
            }

            Path filePath = uploadPath.resolve(fileName).normalize();
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            return fileName;
        } catch (IOException ex) {
            throw new RuntimeException("Failed to store file " + file.getOriginalFilename(), ex);
        }
    }



    @Override
    public void deleteFile(String directory, String fileName) {
        String dirPath;
        switch (directory) {
            case "photo-car":
                dirPath = photoCarDir;
                break;
            case "licence":
                dirPath = licenceDir;
                break;
            case "identityCard":
                dirPath = identityDir;
                break;
            case "registrationCard":
                dirPath = registrationCardDir;
                break;
            case "icons":
                dirPath = iconsDir;
                break;
            case "profil":
                dirPath = profilDir;
                break;
            default:
                throw new IllegalArgumentException("Invalid directory: " + directory);
        }

        try {
            Path filePath = Paths.get(dirPath).resolve(fileName).normalize();
            Files.deleteIfExists(filePath);
        } catch (IOException ex) {
            throw new RuntimeException("Could not delete file: " + fileName, ex);
        }
    }

    public Resource loadFileAsResource(String filePath) {
        try {
            // Résoudre le chemin à partir du répertoire de base des fichiers
            Path fileStorageLocation = Paths.get("src/main/resources/static").resolve(filePath).normalize();
            Resource resource = new UrlResource(fileStorageLocation.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("File not found: " + filePath);
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("File not found: " + filePath, e);
        }
    }
}
