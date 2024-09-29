package be.iccbxl.tfe.Driveshare.controller;

import be.iccbxl.tfe.Driveshare.DTO.*;
import be.iccbxl.tfe.Driveshare.model.*;
import be.iccbxl.tfe.Driveshare.security.CustomUserDetail;
import be.iccbxl.tfe.Driveshare.service.serviceImpl.*;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
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
        int confirmedRentalsAsTenant = 0; // Nombre de locations confirmées en tant que locataire
        long carCount = 0; // Nombre de voitures enregistrées par l'utilisateur
        int confirmedRentalsAsOwner = 0; // Nombre de locations pour ses voitures en tant que propriétaire

        if (userDetails != null) {
            User user = userDetails.getUser();

            // Récupérer le nombre de locations confirmées en tant que locataire
            if (user.getReservations() != null) {
                for (Reservation reservation : user.getReservations()) {
                    if ("CONFIRMED".equalsIgnoreCase(reservation.getStatut()) ||
                            "NOW".equalsIgnoreCase(reservation.getStatut()) ||
                            "FINISHED".equalsIgnoreCase(reservation.getStatut())) {
                        confirmedRentalsAsTenant++;
                    }
                }
            }

            // Récupérer les voitures enregistrées par l'utilisateur
            List<Car> userCars = carService.getCarsByUser(user); // Récupère toutes les voitures de l'utilisateur
            carCount = userCars.size(); // Compter le nombre de voitures

            // Récupérer les locations confirmées en tant que propriétaire (pour les voitures de l'utilisateur)
            for (Car car : userCars) {
                for (Reservation reservation : car.getReservations()) {
                    if ("CONFIRMED".equalsIgnoreCase(reservation.getStatut()) ||
                            "NOW".equalsIgnoreCase(reservation.getStatut()) ||
                            "FINISHED".equalsIgnoreCase(reservation.getStatut())) {
                        confirmedRentalsAsOwner++;
                    }
                }
            }

            // Ajouter les compteurs au modèle
            model.addAttribute("confirmedRentalsAsTenant", confirmedRentalsAsTenant);
            model.addAttribute("carCount", carCount);
            model.addAttribute("confirmedRentalsAsOwner", confirmedRentalsAsOwner);
            model.addAttribute("user", user);
            model.addAttribute("requestURI", request.getRequestURI());
        }

        return "account/index"; // Retourne la vue "account/index"
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

        return "redirect:/account/info";
    }


    @GetMapping("account/document-identity")
    public String document(@AuthenticationPrincipal CustomUserDetail userDetails, Model model, HttpServletRequest request) {
        User user = userDetails.getUser();
        List<Document> documents = documentService.getByUserId(user.getId());

        // Vérification des documents d'identité
        boolean hasIdentityRecto = documents.stream().anyMatch(doc -> "identity_recto".equals(doc.getDocumentType()));
        boolean hasIdentityVerso = documents.stream().anyMatch(doc -> "identity_verso".equals(doc.getDocumentType()));

        // Vérification des documents de permis de conduire
        boolean hasLicenceRecto = documents.stream().anyMatch(doc -> "licence_recto".equals(doc.getDocumentType()));
        boolean hasLicenceVerso = documents.stream().anyMatch(doc -> "licence_verso".equals(doc.getDocumentType()));

        // Ajout des attributs au modèle
        model.addAttribute("user", user);
        model.addAttribute("hasIdentityRecto", hasIdentityRecto);
        model.addAttribute("hasIdentityVerso", hasIdentityVerso);
        model.addAttribute("hasLicenceRecto", hasLicenceRecto);
        model.addAttribute("hasLicenceVerso", hasLicenceVerso);
        model.addAttribute("requestURI", request.getRequestURI());

        return "account/document-identity";
    }


    @PostMapping("/account/document-identity")
    public String uploadDocument(@AuthenticationPrincipal CustomUserDetail userDetails,
                                 @RequestParam(value = "identityRecto", required = false) MultipartFile identityRecto,
                                 @RequestParam(value = "identityVerso", required = false) MultipartFile identityVerso,
                                 @RequestParam(value = "licenceRecto", required = false) MultipartFile licenceRecto,
                                 @RequestParam(value = "licenceVerso", required = false) MultipartFile licenceVerso,
                                 RedirectAttributes redirectAttributes) {
        User user = userDetails.getUser();

        try {
            // Gestion des documents d'identité
            if (identityRecto != null && !identityRecto.isEmpty()) {
                String identityRectoPath = fileStorageService.storeFile(identityRecto, "identityCard");
                Document identityRectoDoc = new Document();
                identityRectoDoc.setUser(user);
                identityRectoDoc.setDocumentType("identity_recto");
                identityRectoDoc.setUrl(identityRectoPath);
                documentService.save(identityRectoDoc);
            }
            if (identityVerso != null && !identityVerso.isEmpty()) {
                String identityVersoPath = fileStorageService.storeFile(identityVerso, "identityCard");
                Document identityVersoDoc = new Document();
                identityVersoDoc.setUser(user);
                identityVersoDoc.setDocumentType("identity_verso");
                identityVersoDoc.setUrl(identityVersoPath);
                documentService.save(identityVersoDoc);
            }

            // Gestion des permis de conduire
            if (licenceRecto != null && !licenceRecto.isEmpty()) {
                String licenceRectoPath = fileStorageService.storeFile(licenceRecto, "licence");
                Document licenceRectoDoc = new Document();
                licenceRectoDoc.setUser(user);
                licenceRectoDoc.setDocumentType("licence_recto");
                licenceRectoDoc.setUrl(licenceRectoPath);
                documentService.save(licenceRectoDoc);
            }
            if (licenceVerso != null && !licenceVerso.isEmpty()) {
                String licenceVersoPath = fileStorageService.storeFile(licenceVerso, "licence");
                Document licenceVersoDoc = new Document();
                licenceVersoDoc.setUser(user);
                licenceVersoDoc.setDocumentType("licence_verso");
                licenceVersoDoc.setUrl(licenceVersoPath);
                documentService.save(licenceVersoDoc);
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

        // Récupérer les notifications et les réclamations pour l'utilisateur
        List<Notification> notifications = notificationService.getNotificationsForUser(user.getId());
        List<Claim> claims = claimService.getClaimsForUser(user.getId());

        // Convertir les entités en DTOs
        List<NotificationDTO> notificationsNonLues = notifications.stream()
                .filter(n -> Boolean.FALSE.equals(n.getIsRead())) // Filtrer les non-lues
                .map(MapperDTO::toNotificationDTO) // Mapper les notifications en DTOs
                .collect(Collectors.toList());

        List<NotificationDTO> notificationsLues = notifications.stream()
                .filter(n -> Boolean.TRUE.equals(n.getIsRead())) // Filtrer les lues
                .map(MapperDTO::toNotificationDTO) // Mapper les notifications en DTOs
                .collect(Collectors.toList());

        List<ClaimDTO> claimsDTOs = claims.stream()
                .map(MapperDTO::toClaimDto) // Mapper les réclamations en DTOs
                .collect(Collectors.toList());

        // Ajouter les objets à passer à la vue
        model.addAttribute("user", user);
        model.addAttribute("notificationsNonLues", notificationsNonLues);
        model.addAttribute("notificationsLues", notificationsLues);
        model.addAttribute("claims", claimsDTOs);
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
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);

        return "account/gains";
    }


    @PostMapping("/account/gains/pdf")
    public ResponseEntity<InputStreamResource> generateGainsPdf(
            @RequestParam("start_date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("end_date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Model model) throws Exception {

        User currentUser = userService.getAuthenticatedUser(); // Récupérer l'utilisateur connecté
        String fullName = currentUser.getFirstName() + " " + currentUser.getLastName();

        // Convertir les LocalDate en LocalDateTime pour les bornes de début et de fin
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        // Récupérer les gains pour l'utilisateur courant dans la plage de dates spécifiée
        List<Gain> gains = gainService.getGainsForUserByDateRange(currentUser.getId(), startDateTime, endDateTime);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(out);
        com.itextpdf.kernel.pdf.PdfDocument pdfDoc = new com.itextpdf.kernel.pdf.PdfDocument(writer);
        com.itextpdf.layout.Document document = new com.itextpdf.layout.Document(pdfDoc);

        // Ajouter le logo de DriveShare
        ClassPathResource logoResource = new ClassPathResource("static/images/DriveShareLogo.png");

        // Utilisez ImageDataFactory.create() avec un InputStream pour créer l'image
        Image logo = new Image(ImageDataFactory.create(logoResource.getInputStream().readAllBytes()));
        logo.setWidth(100);
        document.add(logo);

        // Ajouter le titre et les informations de l'utilisateur
        document.add(new Paragraph("Résumé des gains").setBold().setFontSize(16));
        document.add(new Paragraph("Nom complet : " + fullName));
        document.add(new Paragraph("Période : du " + startDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + " au " + endDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))));

        // Ajouter un tableau des gains
        float[] columnWidths = {200, 200, 200, 200};
        Table table = new Table(columnWidths);

        // En-tête du tableau
        table.addHeaderCell(new Cell().add(new Paragraph("Voiture").setBold()));
        table.addHeaderCell(new Cell().add(new Paragraph("Dates de réservation").setBold()));
        table.addHeaderCell(new Cell().add(new Paragraph("Montant du gain").setBold()));
        table.addHeaderCell(new Cell().add(new Paragraph("Date du versement").setBold()));

        // Remplir les lignes du tableau
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        for (Gain gain : gains) {
            table.addCell(new Cell().add(new Paragraph(gain.getPayment().getReservation().getCar().getBrand() + " " + gain.getPayment().getReservation().getCar().getModel())));
            table.addCell(new Cell().add(new Paragraph(gain.getPayment().getReservation().getStartLocation().format(dateFormatter) + " au " + gain.getPayment().getReservation().getEndLocation().format(dateFormatter))));
            table.addCell(new Cell().add(new Paragraph(String.format("%.2f €", gain.getMontantGain()))));
            table.addCell(new Cell().add(new Paragraph(gain.getDateGain().format(dateFormatter))));
        }

        document.add(table);
        document.close();

        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "inline; filename=gains.pdf");

        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(in));
    }





    @GetMapping("/account/cars")
    public String getCars(Model model, @AuthenticationPrincipal CustomUserDetail userDetails, HttpServletRequest request) {
        if (userDetails != null) {
            User user = userDetails.getUser();

            // Récupérer les voitures de l'utilisateur et les mapper en CarDTO
            List<CarDTO> userCarDTOs = userService.getUserById(user.getId())
                    .getOwnedCars()
                    .stream()
                    .map(MapperDTO::toCarDTO) // Mapper les entités Car vers des CarDTO
                    .collect(Collectors.toList());

            // Ajouter les informations au modèle pour les passer à la vue
            model.addAttribute("user", user);
            model.addAttribute("cars", userCarDTOs); // Utilisation de CarDTO ici
            model.addAttribute("requestURI", request.getRequestURI());

            return "account/cars"; // Vue où les voitures seront affichées
        } else {
            // Si l'utilisateur n'est pas connecté, rediriger vers la page de connexion
            return "redirect:/login";
        }
    }



    @GetMapping("/account/cars/{id}")
    public String getCarDetails(@PathVariable("id") Long carId, Model model,
                                @AuthenticationPrincipal CustomUserDetail userDetails, HttpServletRequest request) {
        if (userDetails != null) {
            User user = userDetails.getUser();

            // Récupérer l'utilisateur actualisé et mapper vers DTO
            User refreshedUser = userService.getUserById(user.getId());
            UserDTO userDTO = MapperDTO.toUserDTO(refreshedUser);

            // Chercher la voiture avec l'id spécifié
            List<CarDTO> ownedCars = userDTO.getOwnedCars();
            System.out.println("Voitures possédées par l'utilisateur : " + ownedCars);
            if (ownedCars != null) {
                Optional<CarDTO> optionalCarDTO = ownedCars.stream()
                        .filter(car -> car.getId() != null && car.getId().equals(carId))
                        .findFirst();

                if (optionalCarDTO.isPresent()) {
                    CarDTO carDTO = optionalCarDTO.get();

                    // Formatage de la date pour l'année
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy");
                    String formattedYear = carDTO.getUser().getCreatedAt().format(formatter);
                    model.addAttribute("formattedYear", formattedYear);

                    // Ajouter les informations de la voiture et de l'utilisateur
                    model.addAttribute("car", carDTO);
                    model.addAttribute("carId", carId);
                    model.addAttribute("user", userDTO); // Utiliser le DTO
                    model.addAttribute("requestURI", request.getRequestURI());

                    // Logique de complétion d'annonce
                    model.addAttribute("hasPersonalInfo", userService.hasPersonalInfo(refreshedUser));
                    model.addAttribute("hasBankInfo", userService.hasBankInfo(refreshedUser));
                    model.addAttribute("hasIdentityInfo", userService.hasIdentityInfo(refreshedUser));
                    model.addAttribute("hasPhotos", userService.hasPhotos(carDTO));
                    model.addAttribute("hasRegistrationCard", userService.hasRegistrationCard(carDTO));
                    model.addAttribute("isPendingValidation", userService.isPendingValidation(carDTO));

                    return "account/car-show"; // Vue
                } else {
                    System.out.println("Voiture non trouvée.");
                    return "redirect:/account/cars";
                }
            } else {
                // Gérer le cas où l'utilisateur n'a pas de voitures
                System.out.println("L'utilisateur n'a pas de voitures.");
                return "redirect:/account/cars";
            }
        } else {
            return "redirect:/login";
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



    @PostMapping("/car/update/reservation")
    public String updateBookingMode(@RequestParam("vehicle_id") Long vehicleId,
                                    @RequestParam("booking_mode") String bookingMode,
                                    RedirectAttributes redirectAttributes) {
        Car car = carService.getCarById(vehicleId);
        car.setModeReservation(bookingMode); // Mettre à jour le mode de réservation
        carService.updateCar(car.getId(), car); // Enregistrer les modifications

        // Ajouter un message flash pour le succès
        redirectAttributes.addFlashAttribute("successMessage", "Le mode de réservation a été mis à jour avec succès.");

        // Rediriger vers la même page avec un paramètre indiquant que l'onglet "reservation-tab" doit être actif
        return "redirect:/account/cars/" + vehicleId + "?activeTab=reservation-tab";
    }



    @PostMapping("/car/update/tarification")
    public String updateVehiclePricing(@RequestParam("vehicle_id") Long vehicleId,
                                       @RequestParam("low_season_price") double lowPrice,
                                       @RequestParam("medium_season_price") double middlePrice,
                                       @RequestParam("high_season_price") double highPrice,
                                       @RequestParam("promo1") double promo1,
                                       @RequestParam("promo2") double promo2,
                                       RedirectAttributes redirectAttributes) {  // Utiliser RedirectAttributes

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

        // Ajouter un message flash
        redirectAttributes.addFlashAttribute("successMessage", "Tarification mise à jour avec succès.");

        // Redirection vers la même page avec l'onglet "tarification-tab" actif
        return "redirect:/account/cars/" + vehicleId + "?activeTab=tarification-tab";
    }



    /* ---------------------- ONGLET RESERVATIONS ---------------------------*/

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

        // Récupérer les détails de la réservation et de la voiture
        Reservation reservation = reservationService.getReservationById(reservationId);
        Car car = carService.getCarById(carId);

        // Ajouter une vérification si une plainte existe déjà
        boolean complaintExists = claimService.existsByReservationId(reservationId);

        // Détails de la voiture
        String reservationStatus = reservation.getStatut();
        String carBrand = car.getBrand();
        String carModel = car.getModel();
        String carFuel = car.getFuelType();
        String carImageUrl = (car.getPhotos() != null && !car.getPhotos().isEmpty())
                ? "/uploads/photo-car/" + car.getPhotos().get(0).getUrl()
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

        // Vérification de l'évaluation existante
        boolean evaluationExists = evaluationService.evaluationExists(reservationId);

        // Calculer la durée de la location
        long duration = ChronoUnit.DAYS.between(debutLocation, finLocation);

        // Calculer le montant total et le montant restant
        double totalPrice = duration * reservation.getCar().getPrice().getMiddlePrice(); // Prix total calculé par jour
        double remainingAmount = 0;

        if ("PAYMENT_PENDING".equals(reservationStatus) || "RESPONSE_PENDING".equals(reservationStatus)) {
            remainingAmount = totalPrice;  // Si le paiement ou la réponse est en attente, le montant restant est égal au prix total
        } else {
            remainingAmount = 0;  // Si la réservation est déjà payée ou annulée, le montant restant est de 0
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
        model.addAttribute("remainingAmount", remainingAmount);
        model.addAttribute("evaluationExists", evaluationExists);
        model.addAttribute("complaintExists", complaintExists);

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
