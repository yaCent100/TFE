package be.iccbxl.tfe.Driveshare.controller;

import be.iccbxl.tfe.Driveshare.DTO.CarDTO;
import be.iccbxl.tfe.Driveshare.model.*;
import be.iccbxl.tfe.Driveshare.security.CustomUserDetail;
import be.iccbxl.tfe.Driveshare.service.serviceImpl.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;


@Controller
@SessionAttributes("auth")
public class AccountController {

    private final UserService userService;
    private final PhotoService photoService;
    private final CarService carService;
    private final FileStorageService fileStorageService;
    private final DocumentService documentService;
    private final NotificationPreferenceService preferenceService;
    private final BCryptPasswordEncoder passwordEncoder;
    private final PaymentService paymentService;
    private final DateService dateService;
    private final CategoryService categoryService;
    private final FeatureService featureService;
    private final EquipmentService equipmentService;

    private final ReservationService reservationService;



    @Autowired
    public AccountController(UserService userService, PriceService priceService, PhotoService photoService, CarService carService, FileStorageService fileStorageService, DocumentService documentService, NotificationPreferenceService preferenceService, BCryptPasswordEncoder passwordEncoder, PaymentService paymentService, DateService dateService, CategoryService categoryService, FeatureService featureService, EquipmentService equipmentService, ReservationService reservationService) {
        this.userService = userService;
        this.photoService = photoService;
        this.carService = carService;
        this.fileStorageService = fileStorageService;
        this.documentService = documentService;
        this.preferenceService = preferenceService;
        this.passwordEncoder = passwordEncoder;
        this.paymentService = paymentService;
        this.dateService = dateService;
        this.categoryService = categoryService;
        this.featureService = featureService;
        this.equipmentService = equipmentService;
        this.reservationService = reservationService;
    }


    @GetMapping("/account")
    public String getAccount(Model model, @AuthenticationPrincipal CustomUserDetail userDetails, HttpServletRequest request) {
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
            model.addAttribute("requestURI", request.getRequestURI());
        }
        return "account/index";
    }


    @GetMapping("account/info")
    public String info(@AuthenticationPrincipal CustomUserDetail userDetails, Model model, HttpServletRequest request) {
        // Ajouter l'utilisateur au modèle
        User user = userDetails.getUser();
        model.addAttribute("user", user);
        model.addAttribute("requestURI", request.getRequestURI());


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
    public String document(@AuthenticationPrincipal CustomUserDetail userDetails, Model model, HttpServletRequest request) {
        User user = userDetails.getUser();
        List<Document> documents = documentService.getByUserId(user.getId());

        boolean hasRecto = documents.stream().anyMatch(doc -> "identity_recto".equals(doc.getDocumentType()));
        boolean hasVerso = documents.stream().anyMatch(doc -> "identity_verso".equals(doc.getDocumentType()));

        model.addAttribute("user", user);
        model.addAttribute("hasRecto", hasRecto);
        model.addAttribute("hasVerso", hasVerso);
        model.addAttribute("requestURI", request.getRequestURI());


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

    @GetMapping("/account/notifications")
    public String showNotificationPreferences(@AuthenticationPrincipal CustomUserDetail userDetails, Model model, HttpServletRequest request) {
        User user = userDetails.getUser();
        NotificationPreference preferences = preferenceService.getPreferences(user);
        if (preferences == null) {
            preferences = new NotificationPreference();
            preferences.setUser(user);
            preferenceService.savePreferences(preferences);
        }
        model.addAttribute("preferences", preferences);
        model.addAttribute("user", user);
        model.addAttribute("requestURI", request.getRequestURI());

        return "account/notification-user";
    }

    @PostMapping("/account/notifications")
    public String saveNotificationPreferences(@AuthenticationPrincipal CustomUserDetail userDetails, @ModelAttribute NotificationPreference preferences) {
        User user = userDetails.getUser();
        NotificationPreference existingPreferences = preferenceService.getPreferences(user);

        if (existingPreferences != null) {
            existingPreferences.setCancelEmailLocataire(preferences.isCancelEmailLocataire());
            existingPreferences.setCancelEmailProprietaire(preferences.isCancelEmailProprietaire());
            existingPreferences.setConfirmedEmailLocataire(preferences.isConfirmedEmailLocataire());
            existingPreferences.setConfirmedEmailProprietaire(preferences.isConfirmedEmailProprietaire());
            existingPreferences.setConfirmedSmsLocataire(preferences.isConfirmedSmsLocataire());
            existingPreferences.setConfirmedSmsProprietaire(preferences.isConfirmedSmsProprietaire());
            existingPreferences.setEndEmailLocataire(preferences.isEndEmailLocataire());
            existingPreferences.setEndEmailProprietaire(preferences.isEndEmailProprietaire());
            existingPreferences.setEndSmsLocataire(preferences.isEndSmsLocataire());
            existingPreferences.setEndSmsProprietaire(preferences.isEndSmsProprietaire());
            existingPreferences.setMessagesEmailLocataire(preferences.isMessagesEmailLocataire());
            existingPreferences.setMessagesEmailProprietaire(preferences.isMessagesEmailProprietaire());
            existingPreferences.setReservationEmailLocataire(preferences.isReservationEmailLocataire());
            existingPreferences.setReservationEmailProprietaire(preferences.isReservationEmailProprietaire());
            existingPreferences.setReservationSmsLocataire(preferences.isReservationSmsLocataire());
            existingPreferences.setReservationSmsProprietaire(preferences.isReservationSmsProprietaire());
            existingPreferences.setStartEmailLocataire(preferences.isStartEmailLocataire());
            existingPreferences.setStartEmailProprietaire(preferences.isStartEmailProprietaire());
            existingPreferences.setStartSmsLocataire(preferences.isStartSmsLocataire());
            existingPreferences.setStartSmsProprietaire(preferences.isStartSmsProprietaire());

            preferenceService.savePreferences(existingPreferences);
        } else {
            preferences.setUser(user);
            preferenceService.savePreferences(preferences);
        }

        return "redirect:/account/notifications?success";
    }

    @GetMapping("/account/change-password")
    public String modifiePassword(@AuthenticationPrincipal CustomUserDetail userDetails, Model model, HttpServletRequest request) {
        User user = userDetails.getUser();

        model.addAttribute("user", user);
        model.addAttribute("requestURI", request.getRequestURI());

        return "account/change-password";
    }

    @PostMapping("/account/change-password")
    public String changePassword(@AuthenticationPrincipal CustomUserDetail userDetails,
                                 @RequestParam("current_password") String currentPassword,
                                 @RequestParam("password") String newPassword,
                                 @RequestParam("password_confirm") String confirmPassword,
                                 RedirectAttributes redirectAttributes) {

        User user = userService.getUserById(userDetails.getUser().getId());

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            redirectAttributes.addFlashAttribute("error", "Le mot de passe actuel est incorrect.");
            return "redirect:/account/change-password";
        }

        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "Les nouveaux mots de passe ne correspondent pas.");
            return "redirect:/account/change-password";
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userService.save(user);

        redirectAttributes.addFlashAttribute("success", "Votre mot de passe a été modifié avec succès.");
        return "redirect:/account/change-password";
    }

    @GetMapping("/account/bank-account")
    public String bankAccount(@AuthenticationPrincipal CustomUserDetail userDetails, Model model, HttpServletRequest request) {
        User user = userDetails.getUser();

        model.addAttribute("user", user);
        model.addAttribute("requestURI", request.getRequestURI());

        return "account/bank-account";
    }

    @PostMapping("/account/bank-account")
    public String updateBankAccount(@AuthenticationPrincipal CustomUserDetail userDetails,
                                    @RequestParam("wallet[iban]") String iban,
                                    @RequestParam("wallet[bic]") String bic,
                                    RedirectAttributes redirectAttributes, Model model) {
        User user = userDetails.getUser();
        user.setIban(iban);
        user.setBic(bic);
        userService.save(user);
        redirectAttributes.addFlashAttribute("success", "Vos informations ont bien été mises à jour.");


        return "redirect:/account/bank-account";
    }

    @GetMapping("/account/gains")
    public String recapGains(@AuthenticationPrincipal CustomUserDetail userDetails, Model model) {
        User user = userDetails.getUser();

        model.addAttribute("user", user);
        return "account/gains";
    }

    @PostMapping("/account/gains")
    public String getSummary(@AuthenticationPrincipal CustomUserDetail userDetails,
                             @RequestParam("start_date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                             @RequestParam("end_date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                             Model model) {
        User user = userDetails.getUser();

        // Ajout de logs pour vérifier les dates reçues
        System.out.println("Date de début: " + startDate);
        System.out.println("Date de fin: " + endDate);

        List<Payment> payments = paymentService.getPaymentsForUser(user, startDate, endDate);
        model.addAttribute("payments", payments);
        model.addAttribute("user", user);


        if (payments.isEmpty()) {
            System.out.println("Aucun paiement trouvé pour les dates sélectionnées.");
        } else {
            System.out.println("Paiements trouvés : " + payments.size());
            for (Payment payment : payments) {
                System.out.println(payment.toString());
            }
        }

        return "account/gains";
    }




    @GetMapping("/account/cars")
    public String getCars(Model model, @AuthenticationPrincipal CustomUserDetail userDetails, HttpServletRequest request) {
        if (userDetails != null) {
            User user = userDetails.getUser();

            List<Car> userCars = userService.getUserById(user.getId()).getOwnedCars();

            // Ajouter les voitures au modèle
            model.addAttribute("user", user);
            model.addAttribute("cars", userCars);
            model.addAttribute("requestURI", request.getRequestURI());

            return "account/cars";
        } else {
            // Gérer le cas où aucun utilisateur n'est connecté
            return "redirect:/login"; // Rediriger vers la page de connexion
        }
    }

    @GetMapping("/account/cars/{id}")
    public String getCarDetails(@PathVariable("id") Long carId, Model model, @AuthenticationPrincipal CustomUserDetail userDetails,
                                HttpServletRequest request) {
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
                model.addAttribute("requestURI", request.getRequestURI());


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

    @GetMapping("/account/cars/edit/{id}")
    public String showEditCarForm(@PathVariable("id") Long id, Model model,  @AuthenticationPrincipal CustomUserDetail userDetails) {
        Car car = carService.getCarById(id);
        User user = userDetails.getUser();

        if (car == null) {
            // Handle car not found
            return "redirect:/account/cars";
        }

        model.addAttribute("user", user);
        model.addAttribute("carDTO", new CarDTO(car));
        model.addAttribute("categories", categoryService.getAllCategory());
        model.addAttribute("boiteFeatures", featureService.findByCategory("Boite"));
        model.addAttribute("compteurFeatures", featureService.findByCategory("Compteur"));
        model.addAttribute("placesFeatures", featureService.findByCategory("Places"));
        model.addAttribute("portesFeatures", featureService.findByCategory("Portes"));
        model.addAttribute("equipments", equipmentService.getAllEquipments());
        model.addAttribute("days", dateService.range(1, 31));
        model.addAttribute("months", dateService.getMonths());
        model.addAttribute("years", dateService.getYears(10));


        return "account/car-edit";
    }

    @PostMapping("/account/cars/edit/{id}")
    public String updateCar(@PathVariable("id") Long id, @ModelAttribute("carDTO") CarDTO carDTO, BindingResult result, Model model ) {


        if (result.hasErrors()) {
            model.addAttribute("categories", categoryService.getAllCategory());
            model.addAttribute("boiteFeatures", featureService.findByCategory("Boite"));
            model.addAttribute("compteurFeatures", featureService.findByCategory("Compteur"));
            model.addAttribute("placesFeatures", featureService.findByCategory("Places"));
            model.addAttribute("portesFeatures", featureService.findByCategory("Portes"));
            model.addAttribute("equipments", equipmentService.getAllEquipments());
            model.addAttribute("days", dateService.range(1, 31));
            model.addAttribute("months", dateService.getMonths());
            model.addAttribute("years", dateService.getYears(10));

            return "account/car-edit";
        }

        carService.updateCar(id, carDTO);
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

    @PostMapping("/car/update/reservation")
    public String updateBookingMode(@RequestParam("vehicle_id") Long vehicleId,
                                    @RequestParam("booking_mode") String bookingMode,
                                    RedirectAttributes redirectAttributes) {
        Car car = carService.getCarById(vehicleId);
        car.setModeReservation(bookingMode);
        carService.updateCar(car.getId(), car);

        // Ajouter un message flash
        redirectAttributes.addFlashAttribute("successMessage", "Le mode de réservation a été mis à jour avec succès.");

        // Rediriger vers la même page avec l'ancre "reservation-tab"
        return "redirect:/account/cars/" + vehicleId + "#reservation-tab";
    }

    @PostMapping("/car/update/tarification")
    public String updateVehiclePricing(@RequestParam("vehicle_id") Long vehicleId,
                                       @RequestParam("low_season_price") double lowPrice,
                                       @RequestParam("medium_season_price") double middlePrice,
                                       @RequestParam("high_season_price") double highPrice,
                                       @RequestParam("promo1") double promo1,
                                       @RequestParam("promo2") double promo2,
                                       Model model) {
        Car car = carService.getCarById(vehicleId);
        Price price = car.getPrice();
        if (price == null) {
            price = new Price();
            car.setPrice(price);
        }
        price.setLowPrice(lowPrice);
        price.setMiddlePrice(middlePrice);
        price.setHighPrice(highPrice);
        price.setPromo1(promo1);
        price.setPromo2(promo2);

        carService.updateCar(car.getId(), car);

        model.addAttribute("successMessage", "Tarification mise à jour avec succès.");
        return "redirect:/account/cars/" + vehicleId + "#tarification-tab";
    }


    /*  ONGLET RESERVATIONS */

    @GetMapping("/account/reservations")
    public String getReservations(@AuthenticationPrincipal CustomUserDetail userDetails, Model model,
                                  HttpServletRequest request) {
        User user = userDetails.getUser();

        // Récupérer les voitures de l'utilisateur
        List<Car> userCars = carService.getCarsByUser(user);

        model.addAttribute("user", user);
        model.addAttribute("userCars", userCars);
        model.addAttribute("requestURI", request.getRequestURI());

        return "account/car-reservation/index";
    }

    @GetMapping("/account/car/{carId}/reservations/{reservationId}")
    public String chatPage(
            @AuthenticationPrincipal CustomUserDetail userDetails,
            @PathVariable Long carId,
            @PathVariable Long reservationId,
            Model model) {

        User user = userDetails.getUser();

        // Récupérer les détails de la réservation
        Reservation reservation = reservationService.getReservationById(reservationId);
        Car car = carService.getCarById(carId);

        // Détails de la voiture
        String reservationStatus = reservation.getStatut();
        String carBrand = car.getBrand();
        String carModel = car.getModel();
        String carFuel = car.getFuelType();
        String carImageUrl = (car.getPhotos() != null && !car.getPhotos().isEmpty())
                ? "/uploads/" + car.getPhotos().get(0).getUrl()
                : "default-car.png";
        String carAddress = car.getAdresse() + ", " + car.getCodePostal() + " " + car.getLocality();

        // Déterminer si l'utilisateur est le propriétaire ou le locataire
        boolean isOwner = user.getId().equals(car.getUser().getId());
        User profileUser = isOwner ? reservation.getCarRental().getUser() : car.getUser();

        // Définir la photo de profil par défaut si elle est manquante
        if (profileUser.getPhotoUrl() == null || profileUser.getPhotoUrl().isEmpty()) {
            profileUser.setPhotoUrl("/uploads/profil/defaultPhoto.png");
        }

        // Conversion des dates en objets LocalDate si nécessaire
        LocalDate debutLocation = reservation.getDebutLocation();
        LocalDate finLocation = reservation.getFinLocation();

        String formattedStartDate = dateService.formatAndCapitalizeDate(debutLocation);
        String formattedEndDate = dateService.formatAndCapitalizeDate(finLocation);

        // Ajouter les attributs au modèle
        model.addAttribute("user", user);
        model.addAttribute("reservationId", reservationId);
        model.addAttribute("carId", carId);
        model.addAttribute("reservationStatus", reservationStatus);
        model.addAttribute("carBrand", carBrand);
        model.addAttribute("carModel", carModel);
        model.addAttribute("carFuel", carFuel);
        model.addAttribute("carImageUrl", carImageUrl);
        model.addAttribute("reservationStartDate", formattedStartDate);
        model.addAttribute("reservationEndDate", formattedEndDate);
        model.addAttribute("carAddress", carAddress);
        model.addAttribute("profileUser", profileUser);
        model.addAttribute("isOwner", isOwner);
        model.addAttribute("toUserId", profileUser.getId());
        model.addAttribute("fromUserId", user.getId());

        return "account/car-reservation/chat";
    }






}
