package be.iccbxl.tfe.Driveshare.service;

import be.iccbxl.tfe.Driveshare.model.User;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface UserServiceI {

    List<User> getAllUsers();
    User getUserById(Long id);
    void addUser(User user);
    void updateUser(User user);
    void deleteUser(Long id);

    String uploadFile(MultipartFile file, String uploadDir) throws IOException;

    long countCarsById(Long userId);

    User save(User user);

    User findByEmail(String email);




}
