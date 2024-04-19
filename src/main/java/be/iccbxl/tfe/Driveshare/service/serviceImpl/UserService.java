package be.iccbxl.tfe.Driveshare.service.serviceImpl;

import be.iccbxl.tfe.Driveshare.model.Role;
import be.iccbxl.tfe.Driveshare.model.User;
import be.iccbxl.tfe.Driveshare.repository.RoleRepository;
import be.iccbxl.tfe.Driveshare.repository.UserRepository;
import be.iccbxl.tfe.Driveshare.service.UserServiceI;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserService implements UserServiceI {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;


    @Override
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        userRepository.findAll().forEach(users::add);
        return users;
    }

    @Override
    public User getUserById(Long id) {
        Optional<User> optionalUser = userRepository.findById(id);
        return optionalUser.orElse(null);
    }

    @Override
    public void addUser(User user) {
        Role defaultRole = roleRepository.findByRole("user");
        try {
            user.addRole(defaultRole);

            userRepository.save(user);
            System.out.println("L'utilisateur a été ajouté avec succès avec le rôle par défaut 'user'.");
        } catch (Exception e) {
            // Log de l'exception
            throw new RuntimeException("Une erreur s'est produite lors de l'ajout de l'utilisateur.", e);
        }

    }

    @Override
    public String uploadFile(MultipartFile file, String uploadDir) throws IOException {
        // Vérifier si le répertoire d'upload existe, sinon le créer
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Récupérer le nom original du fichier
        String originalFileName = file.getOriginalFilename();
        if (originalFileName != null && originalFileName.length() > 0) {
            try {
                // Définir le chemin complet du fichier à enregistrer
                Path filePath = uploadPath.resolve(originalFileName);
                // Enregistrer le fichier sur le disque
                Files.copy(file.getInputStream(), filePath);

                // Retourner l'URL du fichier enregistré, en incluant le dossier spécifique
                return filePath.getFileName().toString();
            } catch (IOException e) {
                throw new IOException("Failed to store file " + originalFileName, e);
            }
        } else {
            throw new IOException("Invalid file name.");
        }
    }


    @Override
    public User updateUser(Long id, User newUser) {
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isPresent()) {
            User existingUser = optionalUser.get();
            existingUser.setNom(newUser.getNom());

            // Add more setters for other fields as needed
            return userRepository.save(existingUser);
        }
        return null;
    }

    @Override
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}
