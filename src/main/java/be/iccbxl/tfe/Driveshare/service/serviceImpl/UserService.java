package be.iccbxl.tfe.Driveshare.service.serviceImpl;

import be.iccbxl.tfe.Driveshare.DTO.CarDTO;
import be.iccbxl.tfe.Driveshare.DTO.MapperDTO;
import be.iccbxl.tfe.Driveshare.DTO.ReservationDataDTO;
import be.iccbxl.tfe.Driveshare.DTO.UserDTO;
import be.iccbxl.tfe.Driveshare.config.WebSocketConfig;
import be.iccbxl.tfe.Driveshare.model.*;
import be.iccbxl.tfe.Driveshare.repository.*;
import be.iccbxl.tfe.Driveshare.security.CustomUserDetail;
import be.iccbxl.tfe.Driveshare.service.UserServiceI;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

@Service
public class UserService implements UserServiceI {

    private final UserRepository userRepository;

    private final RoleRepository roleRepository;

    private final JavaMailSender mailSender;

    private final TokenService tokenService;

    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    private final ConcurrentMap<String, Principal> connectedUsers;

    @Autowired
    public UserService(UserRepository userRepository, RoleRepository roleRepository, JavaMailSender mailSender, TokenService tokenService, BCryptPasswordEncoder bCryptPasswordEncoder, WebSocketConfig webSocketConfig) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.mailSender = mailSender;
        this.tokenService = tokenService;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.connectedUsers = webSocketConfig.getConnectedUsers();
    }

    public void addUser(String username, Principal user) {
        connectedUsers.put(username, user);
    }

    public void removeUser(String username) {
        connectedUsers.remove(username);
    }

    public int countConnectedUsers() {
        System.out.println("Nombre d'utilisateurs connectés: " + connectedUsers);
        return connectedUsers.size();
    }

    public ConcurrentMap<String, Principal> getConnectedUsers() {
        return connectedUsers;
    }


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
        // Vérification si l'email existe déjà
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Cette adresse email est déjà utilisée.");
        }

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

    public UserDTO addUserAdmin(User user) {
        // Vérification si l'email existe déjà
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Cette adresse email est déjà utilisée.");
        }

        // Récupérer le rôle par défaut
        Role defaultRole = roleRepository.findByRole("User");
        if (defaultRole == null) {
            throw new RuntimeException("Le rôle par défaut 'user' n'existe pas dans la base de données.");
        }

        try {
            // Ajouter le rôle par défaut à l'utilisateur
            user.addRole(defaultRole);
            // Hacher le mot de passe
            user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
            // Définir la photo par défaut
            user.setPhotoUrl("defaultPhoto.png");
            // Définir la date de création
            user.setCreatedAt(LocalDateTime.now());
            // Sauvegarder l'utilisateur dans la base de données
            User savedUser = userRepository.save(user);

            // Créer un UserDTO à partir des informations de l'utilisateur sauvegardé
            return new UserDTO(
                    savedUser.getId(),
                    savedUser.getFirstName(),
                    savedUser.getLastName(),
                    savedUser.getAdresse(),
                    savedUser.getLocality(),
                    savedUser.getPostalCode(),
                    savedUser.getEmail(),
                    savedUser.getPassword()
                    );

        } catch (Exception e) {
            // Log de l'exception et re-jet de l'erreur
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
        System.out.println("Authentication: " + principal);  // Log pour déboguer le principal
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


    public List<User> findByRole(String role) {
        return userRepository.findByRole(role);
    }


    public List<String> getUserRoles(Long userId) {
        // Récupérer l'utilisateur avec ses rôles
        return userRepository.findById(userId)
                .map(user -> user.getRoles().stream()
                        .map(Role::getRole)
                        .collect(Collectors.toList()))
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
    }


    public List<ReservationDataDTO> getRegistrationsByMonth() {
        // Exemple: Regrouper les utilisateurs par mois et compter
        List<ReservationDataDTO> registrationData = userRepository.findRegistrationsByMonth();
        return registrationData;
    }

    public List<ReservationDataDTO> getUsersGroupedByLocality() {
        // Supposons que findUsersGroupedByLocality retourne une liste de Map<String, Object>
        List<Map<String, Object>> result = userRepository.findUsersGroupedByLocality();

        // Mapper les résultats en DTOF
        return ReservationDataDTO.toDTOList(result);
    }

    public int countNewRegistrations() {
        // Obtenir la date actuelle et soustraire un mois
        LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);

        // Appeler le dépôt avec la date ajustée
        return userRepository.countNewRegistrationsSince(oneMonthAgo);
    }



    // Total des utilisateurs
    public int countTotalUsers() {
        return (int) userRepository.count();
    }

    // Utilisateurs ayant inscrit une voiture
    public int countUsersWithRegisteredCars() {
        return userRepository.countUsersWithRegisteredCars();
    }



    // Vérifier si l'utilisateur a rempli ses informations personnelles
    public boolean hasPersonalInfo(User user) {
        // Par exemple, vérifiez si certaines informations personnelles sont présentes
        return user.getFirstName() != null && user.getLastName() != null && user.getEmail() != null;
    }

    // Vérifier si l'utilisateur a fourni des informations bancaires
    public boolean hasBankInfo(User user) {
        // Vérifiez si l'utilisateur a des informations bancaires (ajustez selon vos besoins)
        return user.getIban() != null && !user.getIban().isEmpty() || user.getBic()!= null && !user.getBic().isEmpty() ;
    }

    // Vérifier si l'utilisateur a fourni sa pièce d'identité
    public boolean hasIdentityInfo(User user) {
        // Vérifiez si l'utilisateur a téléchargé une pièce d'identité
        return user.getDocuments() != null;
    }

    // Vérifier si des photos du véhicule ont été ajoutées
    public boolean hasPhotos(CarDTO carDTO) {
        // Vérifiez si des photos sont disponibles pour le véhicule
        return carDTO.getPhotoUrl() != null && !carDTO.getPhotoUrl().isEmpty();
    }

    // Vérifier si la carte grise du véhicule est présente
    public boolean hasRegistrationCard(CarDTO carDTO) {
        // Vérifiez si la carte grise a été ajoutée
        return carDTO.getRegistrationCardUrl() != null && !carDTO.getRegistrationCardUrl().isEmpty();
    }

    // Vérifier si la voiture est en attente de validation
    public boolean isPendingValidation(CarDTO carDTO) {
        // Ajoutez votre logique pour déterminer si la voiture est en attente de validation
        return carDTO.getOnline();
    }


    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email); // Cette méthode doit exister dans votre repository
    }


}

