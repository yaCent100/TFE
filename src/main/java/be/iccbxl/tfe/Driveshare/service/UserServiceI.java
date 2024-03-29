package be.iccbxl.tfe.Driveshare.service;

import be.iccbxl.tfe.Driveshare.model.User;

import java.util.List;

public interface UserServiceI {

    List<User> getAllUsers();
    User getUserById(Long id);
    User saveUser(User user);
    User updateUser(Long id, User user);
    void deleteUser(Long id);
}
