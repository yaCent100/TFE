package be.iccbxl.tfe.Driveshare.controller;

import be.iccbxl.tfe.Driveshare.DTO.*;
import be.iccbxl.tfe.Driveshare.model.*;
import be.iccbxl.tfe.Driveshare.security.CustomUserDetail;
import be.iccbxl.tfe.Driveshare.service.serviceImpl.*;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;


@Controller
public class AccountController {

    private final UserService userService;
    private final PhotoService photoService;
    private final GainService gainService;
    private final CarService carService;
    private final FileStorageService fileStorageService;
    private final DocumentService documentService;
    private final BCryptPasswordEncoder passwordEncoder;
    private final PaymentService paymentService;
    private final DateService dateService;
    private final CategoryService categoryService;
    private final FeatureService featureService;
    private final EquipmentService equipmentService;

    private final ReservationService reservationService;
    private final NotificationService notificationService;

    private final EvaluationService evaluationService;
    private final ClaimService claimService;

    private static final Logger logger = LoggerFactory.getLogger(AccountController.class);





    @Autowired
    public AccountController(UserService userService, PhotoService photoService, GainService gainService, CarService carService, FileStorageService fileStorageService, DocumentService documentService,
                             BCryptPasswordEncoder passwordEncoder, PaymentService paymentService, DateService dateService, CategoryService categoryService,
                             FeatureService featureService, EquipmentService equipmentService, ReservationService reservationService, NotificationService notificationService, EvaluationService evaluationService, ClaimService claimService) {
        this.userService = userService;
        this.photoService = photoService;
        this.gainService = gainService;
        this.carService = carService;
        this.fileStorageService = fileStorageService;
        this.documentService = documentService;
        this.passwordEncoder = passwordEncoder;
        this.paymentService = paymentService;
        this.dateService = dateService;
        this.categoryService = categoryService;
        this.featureService = featureService;
        this.equipmentService = equipmentService;
        this.reservationService = reservationService;
        this.notificationService = notificationService;
        this.evaluationService = evaluationService;
        this.claimService = claimService;
    }


    @GetMapping("/account")
    public String getAccount(Model model, @AuthenticationPrincipal CustomUserDetail userDetails, HttpServletRequest request) {
        int confirmedCount = 0; // Initialisez le compteur de réservations confirmées à 0
        long carCount = 0; // Initialisez le compteur de voitures à 0

        if (userDetails != null) {
            User user = userDetails.getUser();

            // Itérez sur les réservations de l'utilisateur pour compter les réservations confirmées
            for (Reservation reservation : user.getReservations()) {
                if ("CONFIRMED".equalsIgnoreCase(reservation.getStatut())) {
                    confirmedCount++;
                }
            }

            // Récupérez le nombre de voitures liées à l'utilisateur depuis la base de données
            carCount = userService.countCarsById(user.getId());

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
        user.setFirstName(prenom);
        user.setLastName(nom);
        user.setEmail(email);
        user.setAdresse(adresse);
        user.setLocality(locality);
        user.setPostalCode(codePostal);
        user.setPhone(telephoneNumber);

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
    public String getNotifications(HttpServletRequest request, Model model, @AuthenticationPrincipal CustomUserDetail userDetails) {
        User user = userDetails.getUser();
        List<Notification> notifications = notificationService.getNotificationsForUser(user.getId());
        List<Claim> claims = claimService.getClaimsForUser(user.getId());


        // Filtrer les notifications non lues et lues tout en gérant le cas où isRead est null
        List<Notification> notificationsNonLues = notifications.stream()
                .filter(n -> Boolean.FALSE.equals(n.getIsRead())) // Gère le cas où isRead est null
                .collect(Collectors.toList());

        List<Notification> notificationsLues = notifications.stream()
                .filter(n -> Boolean.TRUE.equals(n.getIsRead())) // Gère le cas où isRead est null
                .collect(Collectors.toList());

        model.addAttribute("user", user);
        model.addAttribute("notificationsNonLues", notificationsNonLues);
        model.addAttribute("notificationsLues", notificationsLues);
        model.addAttribute("claims", claims);
        model.addAttribute("requestURI", request.getRequestURI());

        return "account/notification-user";
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


  /*  @GetMapping("/account/gains")
    public String getUserGains(Model model, HttpServletRequest request) {
        User currentUser = userService.getAuthenticatedUser(); // Récupérer l'utilisateur connecté
        List<Payment> payments = paymentService.getPaymentsForUser(currentUser, LocalDateTime.MIN, LocalDateTime.MAX);

        List<Map<String, Object>> paymentDetails = new ArrayList<>();
        for (Payment payment : payments) {
            Map<String, Object> details = new HashMap<>();
            PaymentDTO paymentDTO = MapperDTO.toPaymentDTO(payment);
            details.put("payment", paymentDTO);

            // Récupérer la réservation en utilisant l'ID de réservation
            Reservation reservation = reservationService.getReservationById(paymentDTO.getReservationId());
            if (reservation != null) {
                details.put("carImage", !reservation.getCar().getPhotos().isEmpty() ? "/uploads/" + reservation.getCar().getPhotos().get(0).getUrl() : "images/carDefault.png");
                details.put("carBrand", reservation.getCar().getBrand());
                details.put("carModel", reservation.getCar().getModel());
                details.put("debutLocation", reservation.getDebutLocation().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
                details.put("finLocation", reservation.getFinLocation().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
                details.put("reservationStatus", reservation.getStatut());
            } else {
                System.out.println("Reservation not found for Payment ID: " + paymentDTO.getId());
            }
            paymentDetails.add(details);
        }

        System.out.println("Payment Details: " + paymentDetails); // Debug
        model.addAttribute("paymentDetails", paymentDetails);
        model.addAttribute("requestURI", request.getRequestURI());

        return "account/gains";
    }*/

    @GetMapping("/account/gains")
    public String getUserGains(Model model, HttpServletRequest request) {
        User currentUser = userService.getAuthenticatedUser();  // Récupérer l'utilisateur connecté
        List<Gain> gains = gainService.getGainsForUserByDateRange(currentUser.getId(), LocalDateTime.MIN, LocalDateTime.MAX);
        List<GainDTO> gainDTOs = gains.stream()
                .map(MapperDTO::toGainDTO)
                .collect(Collectors.toList());
        model.addAttribute("gains", gainDTOs);
        model.addAttribute("requestURI", request.getRequestURI());

        return "account/gains";
    }

    @PostMapping("/account/gains")
    public String getGainsByDate(
            @RequestParam("start_date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("end_date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Model model) {
        User currentUser = userService.getAuthenticatedUser(); // Récupérer l'utilisateur connecté

        // Convertir les LocalDate en LocalDateTime pour les bornes de début et de fin
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        // Récupérer les gains pour l'utilisateur courant dans la plage de dates spécifiée
        List<Gain> gains = gainService.getGainsForUserByDateRange(currentUser.getId(), startDateTime, endDateTime);
        List<GainDTO> gainDTOs = gains.stream()
                .map(MapperDTO::toGainDTO)
                .collect(Collectors.toList());

        // Ajouter les gains au modèle pour les afficher dans la vue
        model.addAttribute("gains", gainDTOs);
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
        String carAddress = car.getAdresse() + ", " + car.getPostalCode() + " " + car.getLocality();

        // Déterminer si l'utilisateur est le propriétaire ou le locataire
        boolean isOwner = user.getId().equals(car.getUser().getId());
        User profileUser = isOwner ? reservation.getCar().getUser() : car.getUser();

        // Définir la photo de profil par défaut si elle est manquante
        if (profileUser.getPhotoUrl() == null || profileUser.getPhotoUrl().isEmpty()) {
            profileUser.setPhotoUrl("/uploads/profil/defaultPhoto.png");
        }

        // Conversion des dates en objets LocalDate si nécessaire
        LocalDate debutLocation = reservation.getStartLocation();
        LocalDate finLocation = reservation.getEndLocation();

        String formattedStartDate = dateService.formatAndCapitalizeDate(debutLocation);
        String formattedEndDate = dateService.formatAndCapitalizeDate(finLocation);

        boolean evaluationExists = evaluationService.evaluationExists(reservationId);
        model.addAttribute("evaluationExists", evaluationExists);

        // Calculer les détails de la location
        long duration = ChronoUnit.DAYS.between(debutLocation, finLocation);

        double totalPrice = 0;
        if ("PAYMENT_PENDING".equals(reservationStatus)) {
            // Calculer le prix total si la réservation est en attente de paiement
            double pricePerDay = reservation.getCar().getPrice().getMiddlePrice();
            totalPrice = duration * pricePerDay;
        } else if (reservation.getPayment() != null) {
            // Utiliser le prix total de la table Payment s'il existe
            totalPrice = reservation.getPayment().getPrixTotal();
        }

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
        model.addAttribute("duration", duration);
        model.addAttribute("totalPrice", totalPrice);

        return "account/car-reservation/chat";
    }


    @PostMapping("/account/notifications/reply")
    public String replyToNotification(@RequestParam Long notificationId, @RequestParam String replyMessage, Principal principal) {
        logger.debug("Entering replyToNotification");
        logger.debug("Notification ID: " + notificationId);
        logger.debug("Reply Message: " + replyMessage);

        // Obtenez l'utilisateur actuel
        String email = principal.getName();
        User fromUser = userService.findByEmail(email);
        logger.debug("From User: " + fromUser.getEmail());

        // Trouvez la notification d'origine
        Notification originalNotification = notificationService.getNotificationById(notificationId);
        if (originalNotification == null) {
            logger.error("Notification not found with ID: " + notificationId);
            return "redirect:/notifications?error";
        }

        // Créez une nouvelle notification de réponse
        Notification replyNotification = new Notification();
        replyNotification.setCar(originalNotification.getCar());
        replyNotification.setFromUser(fromUser);
        replyNotification.setToUser(originalNotification.getFromUser());
        replyNotification.setMessage(replyMessage);
        replyNotification.setSentAt(LocalDateTime.now());
        replyNotification.setType("reply"); // Assurez-vous de définir le type
        notificationService.save(replyNotification);
        logger.debug("Reply notification saved");

        // Redirigez vers la page des notifications
        return "redirect:/account/notifications";
    }

    @GetMapping("/notifications/filter")
    @ResponseBody
    public String filterNotifications(@RequestParam boolean nouveauxMessages, @RequestParam boolean tousMessages, @RequestParam boolean notifications, Model model) {
        List<Notification> filteredNotifications = notificationService.filterNotifications(nouveauxMessages, tousMessages, notifications);
        model.addAttribute("filteredNotifications", filteredNotifications);
        return "fragments/notifications :: filteredNotifications";
    }










}
