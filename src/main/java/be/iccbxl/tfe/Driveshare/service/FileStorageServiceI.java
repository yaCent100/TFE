package be.iccbxl.tfe.Driveshare.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageServiceI {

    String storeFile(MultipartFile file, String directory);
    void deleteFile(String directory,String fileName);

}
