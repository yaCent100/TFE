package be.iccbxl.tfe.Driveshare.service.serviceImpl;

import be.iccbxl.tfe.Driveshare.model.Role;
import be.iccbxl.tfe.Driveshare.model.User;
import be.iccbxl.tfe.Driveshare.repository.RoleRepository;
import be.iccbxl.tfe.Driveshare.repository.UserRepository;
import be.iccbxl.tfe.Driveshare.security.CustomUserDetail;
import be.iccbxl.tfe.Driveshare.service.UserServiceI;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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
    public long countCarsById(Long userId) {
        return userRepository.countCarsById(userId);
    }

    @Override
    public User save(User user) {
        return userRepository.save(user);
    }


    @Transactional
    @Override
    public void updateUser(User user) {
        userRepository.save(user);
    }

    @Override
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    public User getAuthenticatedUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof CustomUserDetail) {
            return ((CustomUserDetail) principal).getUser();
        } else if (principal instanceof UserDetails) {
            String username = ((UserDetails) principal).getUsername();
            return userRepository.findByEmail(username);
        }
        return null;
    }
}
