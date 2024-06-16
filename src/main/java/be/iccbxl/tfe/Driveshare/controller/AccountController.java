package be.iccbxl.tfe.Driveshare.controller;

import be.iccbxl.tfe.Driveshare.model.*;
import be.iccbxl.tfe.Driveshare.security.CustomUserDetail;
import be.iccbxl.tfe.Driveshare.service.serviceImpl.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;


@Controller
public class AccountController {

    private final UserService userService;
    private final PhotoService photoService;

    private final CarService carService;

    private final FileStorageService fileStorageService;

    private final DocumentService documentService;


    @Autowired
    public AccountController(UserService userService, PriceService priceService, PhotoService photoService, CarService carService, FileStorageService fileStorageService, DocumentService documentService) {
        this.userService = userService;
        this.photoService = photoService;
        this.carService = carService;
        this.fileStorageService = fileStorageService;
        this.documentService = documentService;
    }


    @GetMapping("/account")
    public String getAccount(Model model, @AuthenticationPrincipal CustomUserDetail userDetails) {
        int confirmedCount = 0; // Initialisez le compteur de réservations confirmées à 0
        int carCount = 0; // Initialisez le compteur de voitures à 0

        if (userDetails != null) {
            User user = userDetails.getUser();

            // Itérez sur les réservations de l'utilisateur pour compter les réservations confirmées
            for (CarRental carRental : user.getCarRentals()) {
                for (Reservation reservation : carRental.getReservations()) {
                    if ("confirmé".equals(reservation.getStatut())) {
                        confirmedCount++;
                    }
                }
            }

            // Récupérez le nombre de voitures liées à l'utilisateur depuis la base de données
            carCount = Math.toIntExact(userService.countCarsById(user.getId())); // Suppose que vous avez un repository pour les voitures

            // Ajoutez les compteurs au modèle
            model.addAttribute("confirmedCount", confirmedCount);
            model.addAttribute("carCount", carCount);
            model.addAttribute("user", user);
        }
        return "account/index";
    }

    @GetMapping("account/info")
    public String info(@AuthenticationPrincipal CustomUserDetail userDetails, Model model) {
        // Ajouter l'utilisateur au modèle
        User user = userDetails.getUser();
        model.addAttribute("user", user);

        return "account/info-personnel";
    }

    @PostMapping("account/info")
    public String updateInfo(@AuthenticationPrincipal CustomUserDetail userDetails,
                             @RequestParam("prenom") String prenom,
                             @RequestParam("nom") String nom,
                             @RequestParam("email") String email,
                             @RequestParam("adresse") String adresse,
                             @RequestParam("locality") String locality,
                             @RequestParam("codePostal") String codePostal,
                             @RequestParam("telephoneNumber") String telephoneNumber,
                             RedirectAttributes redirectAttributes) {
        // Mettre à jour les informations de l'utilisateur
        User user = userDetails.getUser();
        user.setPrenom(prenom);
        user.setNom(nom);
        user.setEmail(email);
        user.setAdresse(adresse);
        user.setLocality(locality);
        user.setCodePostal(codePostal);
        user.setTelephoneNumber(telephoneNumber);

        // Sauvegarder les changements dans la base de données
        userService.updateUser(user);

        // Ajouter un message de succès aux attributs de redirection
        redirectAttributes.addFlashAttribute("successMessage", "Vos informations ont été mises à jour avec succès.");

        return "redirect:/account";
    }


    @GetMapping("account/document-identity")
    public String document(@AuthenticationPrincipal CustomUserDetail userDetails, Model model) {
        User user = userDetails.getUser();
        List<Document> documents = documentService.getByUserId(user.getId());

        boolean hasRecto = documents.stream().anyMatch(doc -> "identity_recto".equals(doc.getDocumentType()));
        boolean hasVerso = documents.stream().anyMatch(doc -> "identity_verso".equals(doc.getDocumentType()));

        model.addAttribute("user", user);
        model.addAttribute("hasRecto", hasRecto);
        model.addAttribute("hasVerso", hasVerso);

        return "account/document-identity";
    }

    @PostMapping("/account/document-identity")
    public String uploadDocument(@AuthenticationPrincipal CustomUserDetail userDetails,
                                 @RequestParam(value = "recto", required = false) MultipartFile recto,
                                 @RequestParam(value = "verso", required = false) MultipartFile verso,
                                 RedirectAttributes redirectAttributes) {
        User user = userDetails.getUser();

        try {
            if (recto != null && !recto.isEmpty()) {
                String rectoPath = fileStorageService.storeFile(recto, "licence");
                Document rectoDoc = new Document();
                rectoDoc.setUser(user);
                rectoDoc.setDocumentType("licence_recto");
                rectoDoc.setUrl(rectoPath);
                documentService.save(rectoDoc);
            }
            if (verso != null && !verso.isEmpty()) {
                String versoPath = fileStorageService.storeFile(verso, "licence");
                Document versoDoc = new Document();
                versoDoc.setUser(user);
                versoDoc.setDocumentType("licence_verso");
                versoDoc.setUrl(versoPath);
                documentService.save(versoDoc);
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erreur lors de l'upload des fichiers : " + e.getMessage());
            return "redirect:/account/document-identity";
        }

        redirectAttributes.addFlashAttribute("successMessage", "Documents téléchargés avec succès");

        return "redirect:/account/document-identity";
    }



    @GetMapping("/account/cars")
    public String getCars(Model model, @AuthenticationPrincipal CustomUserDetail userDetails) {
        if (userDetails != null) {
            User user = userDetails.getUser();

            List<Car> userCars = userService.getUserById(user.getId()).getOwnedCars();

            // Ajouter les voitures au modèle
            model.addAttribute("user", user);
            model.addAttribute("cars", userCars);

            return "account/cars";
        } else {
            // Gérer le cas où aucun utilisateur n'est connecté
            return "redirect:/login"; // Rediriger vers la page de connexion
        }
    }

    @GetMapping("/account/cars/{id}")
    public String getCarDetails(@PathVariable("id") Long carId, Model model, @AuthenticationPrincipal CustomUserDetail userDetails) {
        // Vérifier si l'utilisateur est connecté
        if (userDetails != null) {
            // Récupérer l'utilisateur connecté
            User user = userDetails.getUser();

            // Ajouter un log pour vérifier l'utilisateur connecté
            System.out.println("Utilisateur connecté : " + user.getEmail());

            // Recharger les voitures de l'utilisateur depuis la base de données
            User refreshedUser = userService.getUserById(user.getId());
            List<Car> userCars = refreshedUser.getOwnedCars();

            // Ajouter un log pour vérifier les voitures de l'utilisateur
            System.out.println("Voitures de l'utilisateur : " + userCars.size());

            // Rechercher la voiture par ID dans la liste des voitures de l'utilisateur
            Optional<Car> optionalCar = userCars.stream().filter(car -> car.getId().equals(carId)).findFirst();

            // Vérifier si la voiture avec l'ID spécifié existe pour cet utilisateur
            if (optionalCar.isPresent()) {
                Car car = optionalCar.get();

                // Ajouter un log pour vérifier la voiture trouvée
                System.out.println("Voiture trouvée : " + car.getModel());

                // Ajouter les détails de la voiture au modèle
                model.addAttribute("car", car);
                model.addAttribute("user", user);

                return "account/car-show"; // Page des détails de la voiture
            } else {
                // Gérer le cas où la voiture n'est pas trouvée pour cet utilisateur
                System.out.println("Voiture non trouvée pour cet utilisateur.");
                return "redirect:/account/cars"; // Rediriger vers la liste des voitures de l'utilisateur
            }
        } else {
            // Gérer le cas où aucun utilisateur n'est connecté
            return "redirect:/login"; // Rediriger vers la page de connexion
        }
    }


    @GetMapping("/account/cars/{id}/delete")
    public String deleteCar(@PathVariable("id") Long carId, @AuthenticationPrincipal CustomUserDetail userDetails, RedirectAttributes redirectAttributes) {
        System.out.println("Received request to delete car with ID: " + carId);

        if (userDetails != null) {
            User user = userDetails.getUser();
            Car car = carService.getCarById(carId);

            if (car != null) {
                if (car.getUser().equals(user)) {
                    carService.deleteCar(carId);
                    redirectAttributes.addFlashAttribute("successMessage", "la voiture a bien été supprimé.");
                } else {
                    redirectAttributes.addFlashAttribute("errorMessage", "You are not authorized to delete this car.");
                }
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Car not found.");
            }
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "You need to be logged in to delete a car.");
        }
        return "redirect:/account/cars";
    }


   /* @PostMapping("/account/cars/addPhoto")
    public String addPhoto(@RequestParam("photo") MultipartFile photo, @RequestParam("carId") Long carId, @AuthenticationPrincipal CustomUserDetail userDetails, RedirectAttributes redirectAttributes) {
        // Save the photo and get the URL
        String photoUrl = fileStorageService.storeFile(photo, "photo");

        // Create a new Photo entity and link it to the car
        Car car = carService.getCarById(carId);

        if (!car.getUser().getId().equals(userDetails.getUser().getId())) {
            redirectAttributes.addFlashAttribute("error", "Unauthorized action");
            return "redirect:/account/cars/" + carId;
        }

        Photo newPhoto = new Photo();
        newPhoto.setUrl(photoUrl);
        newPhoto.setCar(car);
        photoService.savePhoto(newPhoto);

        redirectAttributes.addFlashAttribute("success", "Photo added successfully");
        return "redirect:/account/cars/" + carId;
    }

    @PostMapping("/account/cars/removePhoto/{id}")
    public String removePhoto(@PathVariable("id") Long photoId, @AuthenticationPrincipal CustomUserDetail userDetails, RedirectAttributes redirectAttributes) {
        // Find and delete the photo
        Photo photo = photoService.getPhotoById(photoId);
        if (photo != null && photo.getCar().getUser().getId().equals(userDetails.getUser().getId())) {
            photoService.deletePhoto(photoId);
            fileStorageService.deleteFile(photo.getUrl());
            redirectAttributes.addFlashAttribute("success", "Photo deleted successfully");
        } else {
            redirectAttributes.addFlashAttribute("error", "Unauthorized action");
        }
        return "redirect:/account/cars/" + photo.getCar().getId();
    }*/






}
