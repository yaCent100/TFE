package be.iccbxl.tfe.Driveshare.service.serviceImpl;

import be.iccbxl.tfe.Driveshare.DTO.MapperDTO;
import be.iccbxl.tfe.Driveshare.DTO.UserDTO;
import be.iccbxl.tfe.Driveshare.model.*;
import be.iccbxl.tfe.Driveshare.repository.*;
import be.iccbxl.tfe.Driveshare.security.CustomUserDetail;
import be.iccbxl.tfe.Driveshare.service.UserServiceI;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class UserService implements UserServiceI {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;




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

    public List<UserDTO> getAllUsersDTO() {
        List<User> users = (List<User>) userRepository.findAll();
        return users.stream().map(user -> {
            UserDTO userDTO = MapperDTO.toDTO(user);
            return userDTO;
        }).collect(Collectors.toList());
    }

    public List<UserDTO> getUsersByRole(String role) {
        List<User> users = userRepository.findByRole(role);
        System.out.println("Retrieved users with role " + role + ": " + users); // Log the retrieved users
        return users.stream().map(MapperDTO::toDTO).collect(Collectors.toList());
    }

    @Override
    public void addUser(User user) {
        Role defaultRole = roleRepository.findByRole("user");
        try {
            user.addRole(defaultRole);
            user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
            user.setPhotoUrl("defaultPhoto.png");
            user.setCreatedAt(LocalDateTime.now());
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

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }


    @Transactional
    @Override
    public void updateUser(User user) {
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        try {
            userRepository.deleteById(id);
            System.out.println("Utilisateur supprimé avec l'ID: " + id);
        } catch (Exception e) {
            System.err.println("Erreur lors de la suppression de l'utilisateur: " + e.getMessage());
            e.printStackTrace();
        }
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


    // METHODE POUR INTERFACE ADMIN


    public long getTotalUsers() {
        return userRepository.count();
    }

    public long getNewUsersThisMonth() {
        LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1);
        return userRepository.countByCreatedAtAfter(startOfMonth);
    }

    public List<Integer> getUserGrowth() {
        // Assume this method returns user counts per month for the last 12 months
        return userRepository.getUserGrowthPerMonth();
    }

    public List<User> getRecentUsers() {
        return userRepository.findTop10ByOrderByCreatedAtDesc();
    }

    public boolean sendPasswordResetEmail(String email) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            return false;
        }

        // Générer un token de réinitialisation de mot de passe
        String token = tokenService.createToken(email);

        // Envoyer l'email de réinitialisation
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(user.getEmail());
        mailMessage.setSubject("Réinitialisation de mot de passe");
        mailMessage.setText("Pour réinitialiser votre mot de passe, cliquez sur le lien suivant : "
                + "http://localhost:8080/reset-password?token=" + token);

        mailSender.send(mailMessage);
        return true;
    }


    public void changeUserPassword(User user, String password) {
        String encodedPassword = bCryptPasswordEncoder.encode(password);
        user.setPassword(encodedPassword);
        userRepository.save(user);
    }


    public void updateUserPermissions(Long userId, List<String> permissions) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        List<Role> roles = permissions.stream().map(roleName -> {
            Role role = roleRepository.findByRole(roleName);
            if (role == null) {
                throw new RuntimeException("Role not found: " + roleName);
            }
            return role;
        }).collect(Collectors.toList()); // Collect to List
        user.setRoles(roles); // Set as List
        userRepository.save(user);
    }



}
