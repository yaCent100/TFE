package be.iccbxl.tfe.Driveshare.DTO;

import be.iccbxl.tfe.Driveshare.DTO.*;
import be.iccbxl.tfe.Driveshare.model.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class MapperDTO {

    // Méthode de transformation Entité -> DTO
    public static CarDTO toCarDTO(Car car) {
        CarDTO carDTO = new CarDTO();

        carDTO.setId(car.getId());
        carDTO.setBrand(car.getBrand());
        carDTO.setCodePostal(car.getCodePostal());
        carDTO.setLocality(car.getLocality());
        carDTO.setCategoryId(car.getCategory().getId());
        carDTO.setCategoryName(car.getCategory().getCategory());
        carDTO.setModel(car.getModel());
        carDTO.setAdresse(car.getAdresse());
        carDTO.setOnline(car.getOnline());

        // Gérer les valeurs nulles pour latitude et longitude
        carDTO.setLatitude(car.getLatitude() != null ? car.getLatitude() : 0.0);
        carDTO.setLongitude(car.getLongitude() != null ? car.getLongitude() : 0.0);

        carDTO.setPhotoUrl(car.getPhotos().stream().map(Photo::getUrl).collect(Collectors.toList()));
        carDTO.setPrice(toPriceDTO(car.getPrice()));
        carDTO.setReservationDTOS(car.getReservations().stream().map(MapperDTO::toReservationDTO).collect(Collectors.toList()));
        carDTO.setUnavailables(car.getUnavailable().stream().map(MapperDTO::toIndisponibleDTO).collect(Collectors.toList()));
        carDTO.setEquipments(car.getEquipments().stream().map(MapperDTO::toEquipmentDTO).collect(Collectors.toList()));
        carDTO.setConditionsDTOs(car.getConditions().stream().map(MapperDTO::toConditionDTO).collect(Collectors.toList()));

        // Conversion de la liste des features
        List<FeatureDTO> featureDTOs = car.getFeatures().stream()
                .map(feature -> new FeatureDTO(feature.getId(), feature.getName(), feature.getDescription()))
                .collect(Collectors.toList());
        carDTO.setFeatures(featureDTOs);

        // Ajouter les champs manquants
        carDTO.setFuelType(car.getFuelType());
        carDTO.setPlaqueImmatriculation(car.getPlaqueImmatriculation());
        carDTO.setFirstImmatriculation(car.getFirstImmatriculation());
        carDTO.setCarteGrisePath(car.getCarteGrisePath());
        carDTO.setModeReservation(car.getModeReservation());
        carDTO.setDay(car.getFirstImmatriculation() != null ? car.getFirstImmatriculation().getDayOfMonth() : null);
        carDTO.setMonth(car.getFirstImmatriculation() != null ? car.getFirstImmatriculation().getMonthValue() : null);
        carDTO.setYear(car.getFirstImmatriculation() != null ? car.getFirstImmatriculation().getYear() : null);
        carDTO.setBoiteId(car.getFeatures().stream().filter(f -> f.getName().equals("boite")).findFirst().map(Feature::getId).orElse(null));
        carDTO.setCompteurId(car.getFeatures().stream().filter(f -> f.getName().equals("compteur")).findFirst().map(Feature::getId).orElse(null));
        carDTO.setPlacesId(car.getFeatures().stream().filter(f -> f.getName().equals("places")).findFirst().map(Feature::getId).orElse(null));
        carDTO.setPortesId(car.getFeatures().stream().filter(f -> f.getName().equals("portes")).findFirst().map(Feature::getId).orElse(null));
        carDTO.setEquipmentIds(car.getEquipments().stream().map(Equipment::getId).collect(Collectors.toList()));

        return carDTO;
    }

    public static Car toEntity(CarDTO carDTO) {
        if (carDTO == null) {
            return null;
        }
        Car car = new Car();
        car.setId(carDTO.getId());
        car.setBrand(carDTO.getBrand());
        car.setCodePostal(carDTO.getCodePostal());
        car.setLocality(carDTO.getLocality());
        car.setModel(carDTO.getModel());
        car.setAdresse(carDTO.getAdresse());
        car.setLatitude(carDTO.getLatitude());
        car.setLongitude(carDTO.getLongitude());
        car.setPhotos(carDTO.getPhotoUrl().stream().map(url -> {
            Photo photo = new Photo();
            photo.setUrl(url);
            return photo;
        }).collect(Collectors.toList()));
        car.setPrice(toPrice(carDTO.getPrice()));
        car.setReservations(carDTO.getReservationDTOS().stream().map(MapperDTO::toReservationEntity).collect(Collectors.toList()));
        car.setUnavailable(carDTO.getUnavailables().stream().map(MapperDTO::toIndisponibleEntity).collect(Collectors.toList()));

        return car;
    }

    // Méthode de transformation Entité -> DTO pour Equipment
    public static EquipmentDTO toEquipmentDTO(Equipment equipment) {
        EquipmentDTO equipmentDTO = new EquipmentDTO();
        equipmentDTO.setId(equipment.getId());
        equipmentDTO.setIcon(equipment.getIcone());
        equipmentDTO.setEquipment(equipment.getEquipment());
        return equipmentDTO;
    }

    // Méthode de transformation DTO -> Entité pour Equipment
    public static Equipment toEquipment(EquipmentDTO equipmentDTO) {
        Equipment equipment = new Equipment();
        equipment.setId(equipmentDTO.getId());
        equipment.setIcone(equipmentDTO.getIcon());
        equipment.setEquipment(equipmentDTO.getEquipment());
        return equipment;
    }

    // Méthodes de transformation Entité -> DTO pour Reservation
    public static ReservationDTO toReservationDTO(Reservation reservation) {
        ReservationDTO reservationDTO = new ReservationDTO();
        reservationDTO.setId(reservation.getId());
        reservationDTO.setCarId(reservation.getCar().getId());
        reservationDTO.setDebutLocation(reservation.getDebutLocation().toString());
        reservationDTO.setFinLocation(reservation.getFinLocation().toString());
        reservationDTO.setStatut(reservation.getStatut());
        reservationDTO.setCarBrand(reservation.getCar().getBrand());
        reservationDTO.setCarModel(reservation.getCar().getModel());
        reservationDTO.setUserName(reservation.getUser().getPrenom() + " " + reservation.getUser().getNom());
        reservationDTO.setCarPostal(reservation.getCar().getCodePostal());
        reservationDTO.setCarLocality(reservation.getCar().getLocality());
        reservationDTO.setModeReservation(reservation.getCar().getModeReservation());
        reservationDTO.setUserProfileImage(reservation.getUser().getPhotoUrl() != null && !reservation.getUser().getPhotoUrl().isEmpty() ? "/uploads/profil/" + reservation.getUser().getPhotoUrl() : "/uploads/profil/defaultPhoto.png");
        reservationDTO.setCarImage(!reservation.getCar().getPhotos().isEmpty() ? "/uploads/" + reservation.getCar().getPhotos().get(0).getUrl() : "images/carDefault.png");

        // Ajouter le mapping pour les paiements
        if (reservation.getPayment() != null) {
            reservationDTO.setPayment(toPaymentDTO(reservation.getPayment()));
        }
        return reservationDTO;
    }

    public static Reservation toReservationEntity(ReservationDTO reservationDTO) {
        if (reservationDTO == null) {
            return null;
        }
        Reservation reservation = new Reservation();
        reservation.setId(reservationDTO.getId());
        reservation.setCar(new Car());
        reservation.getCar().setId(reservationDTO.getCarId());
        reservation.setDebutLocation(LocalDate.parse(reservationDTO.getDebutLocation()));
        reservation.setFinLocation(LocalDate.parse(reservationDTO.getFinLocation()));
        reservation.setStatut(reservationDTO.getStatut());

        // Ajouter le mapping pour les paiements
        if (reservationDTO.getPayment() != null) {
            reservation.setPayment(toPaymentEntity(reservationDTO.getPayment()));
        }
        return reservation;
    }

    // Méthode pour convertir une entité Condition en ConditionDTO
    public static ConditionDTO toConditionDTO(Condition condition) {
        if (condition == null) {
            return null;
        }

        ConditionDTO conditionDTO = new ConditionDTO();
        conditionDTO.setId(condition.getId());
        conditionDTO.setCondition(condition.getCondition());
        return conditionDTO;
    }

    // Méthode pour convertir un ConditionDTO en entité Condition
    public static Condition toConditionEntity(ConditionDTO conditionDTO) {
        if (conditionDTO == null) {
            return null;
        }
        Condition condition = new Condition();
        condition.setId(conditionDTO.getId());
        condition.setCondition(conditionDTO.getCondition());
        return condition;
    }


    public static IndisponibleDTO toIndisponibleDTO(Indisponible indisponible) {
        IndisponibleDTO indisponibleDTO = new IndisponibleDTO();
        indisponibleDTO.setId(indisponible.getId());
        indisponibleDTO.setStartDate(indisponible.getStartDate());
        indisponibleDTO.setEndDate(indisponible.getEndDate());
        return indisponibleDTO;
    }

    public static Indisponible toIndisponibleEntity(IndisponibleDTO indisponibleDTO) {
        if (indisponibleDTO == null) {
            return null;
        }
        Indisponible indisponible = new Indisponible();
        indisponible.setId(indisponibleDTO.getId());
        indisponible.setStartDate(indisponibleDTO.getStartDate());
        indisponible.setEndDate(indisponibleDTO.getEndDate());
        return indisponible;
    }

    public static CarDTO convertToCarDTO(Car car) {
        List<FeatureDTO> featureDTOs = car.getFeatures().stream()
                .map(feature -> new FeatureDTO(feature.getId(), feature.getName(), feature.getDescription()))
                .collect(Collectors.toList());

        return new CarDTO(
                car.getId(),
                car.getBrand(),
                car.getModel(),
                car.getModeReservation(),
                car.getPrice().getMiddlePrice(),
                car.getPhotos().stream().findFirst().map(Photo::getUrl).orElse(null),
                car.getAdresse(),
                car.getCodePostal(),
                car.getLocality(),
                featureDTOs
        );
    }

    public static PriceDTO toPriceDTO(Price price) {
        PriceDTO priceDTO = new PriceDTO();
        priceDTO.setId(price.getId());
        priceDTO.setLowPrice(price.getLowPrice());
        priceDTO.setMiddlePrice(price.getMiddlePrice());
        priceDTO.setHighPrice(price.getHighPrice()); // Correction de highPrice
        priceDTO.setPromo1(price.getPromo1());
        priceDTO.setPromo2(price.getPromo2()); // Correction de promo2
        if (price.getCar() != null) {
            priceDTO.setCarId(price.getCar().getId());
        }
        return priceDTO;
    }

    public static Price toPrice(PriceDTO priceDTO) {
        Price price = new Price();
        price.setId(priceDTO.getId());
        price.setLowPrice(priceDTO.getLowPrice());
        price.setMiddlePrice(priceDTO.getMiddlePrice());
        price.setHighPrice(priceDTO.getHighPrice());
        price.setPromo1(priceDTO.getPromo1());
        price.setPromo2(priceDTO.getPromo2());
        return price;
    }

    public static ChatMessage toChatMessage(ChatMessageDTO chatMessageDTO, Reservation reservation, User fromUser, User toUser) {
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setContent(chatMessageDTO.getContent());
        chatMessage.setReservation(reservation);
        chatMessage.setSentAt(LocalDateTime.now());
        return chatMessage;
    }

    public static ChatMessageDTO toChatMessageDTO(ChatMessage message) {
        ChatMessageDTO dto = new ChatMessageDTO();
        dto.setContent(message.getContent());
        dto.setFromUserId(message.getReservation().getUser().getId());
        dto.setToUserId(message.getReservation().getCar().getUser().getId());
        dto.setReservationId(message.getReservation().getId());
        dto.setFromUserNom(message.getReservation().getUser().getNom());

        Car car = message.getReservation().getCar();
        dto.setCarBrand(car.getBrand());
        dto.setCarModel(car.getModel());
        if (car.getPhotos() != null && !car.getPhotos().isEmpty()) {
            dto.setCarImage(car.getPhotos().get(0).getUrl());
        }
        dto.setSentAt(message.getSentAt());
        return dto;
    }

    public static Notification toNotificationEntity(NotificationDTO dto, User fromUser, User toUser, Car car) {
        Notification notification = new Notification();
        notification.setMessage(dto.getMessage());
        notification.setFromUser(fromUser);
        notification.setToUser(toUser);
        notification.setCar(car);
        notification.setType(dto.getType()); // Assurez-vous que le type est défini correctement
        notification.setDateEnvoi(LocalDateTime.now());
        return notification;
    }

    public static NotificationDTO toNotificationDTO(Notification notification) {
        NotificationDTO dto = new NotificationDTO();
        dto.setMessage(notification.getMessage());
        dto.setFromUserId(notification.getFromUser().getId());
        dto.setToUserId(notification.getToUser().getId());
        dto.setCarId(notification.getCar().getId());
        dto.setType(notification.getType());
        return dto;
    }

    public static UserDTO toDTO(User user) {
        if (user == null) {
            return null;
        }

        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setNom(user.getNom());
        userDTO.setPrenom(user.getPrenom());
        userDTO.setEmail(user.getEmail());
        userDTO.setAdresse(user.getAdresse());
        userDTO.setLocality(user.getLocality());
        userDTO.setCodePostal(user.getCodePostal());
        userDTO.setTelephoneNumber(user.getTelephoneNumber());
        userDTO.setPassword(user.getPassword());
        userDTO.setPhotoUrl(user.getPhotoUrl());
        userDTO.setIban(user.getIban());
        userDTO.setBic(user.getBic());
        userDTO.setCreatedAt(user.getCreatedAt());
        userDTO.setOwnedCars(user.getOwnedCars().stream().map(MapperDTO::toCarDTO).collect(Collectors.toList()));
        userDTO.setRoles(user.getRoles().stream().map(MapperDTO::toRoleDTO).collect(Collectors.toList()));
        userDTO.setReservations(user.getReservations().stream().map(MapperDTO::toReservationDTO).collect(Collectors.toList()));
        userDTO.setDocuments(user.getDocuments().stream().map(MapperDTO::toDocumentDTO).collect(Collectors.toList()));
        userDTO.setNotificationsSent(user.getNotificationsSent().stream().map(MapperDTO::toNotificationDTO).collect(Collectors.toList()));
        userDTO.setNotificationsReceived(user.getNotificationsReceived().stream().map(MapperDTO::toNotificationDTO).collect(Collectors.toList()));

        return userDTO;
    }

    public static User toEntity(UserDTO userDTO) {
        if (userDTO == null) {
            return null;
        }

        User user = new User();
        user.setId(userDTO.getId());
        user.setNom(userDTO.getNom());
        user.setPrenom(userDTO.getPrenom());
        user.setEmail(userDTO.getEmail());
        user.setAdresse(userDTO.getAdresse());
        user.setLocality(userDTO.getLocality());
        user.setCodePostal(userDTO.getCodePostal());
        user.setTelephoneNumber(userDTO.getTelephoneNumber());
        user.setPassword(userDTO.getPassword());
        user.setPhotoUrl(userDTO.getPhotoUrl());
        user.setIban(userDTO.getIban());
        user.setBic(userDTO.getBic());
        user.setCreatedAt(userDTO.getCreatedAt());
        user.setOwnedCars(userDTO.getOwnedCars().stream().map(MapperDTO::toEntity).collect(Collectors.toList()));
        user.setRoles(userDTO.getRoles().stream().map(MapperDTO::toRoleEntity).collect(Collectors.toList()));
        user.setReservations(userDTO.getReservations().stream().map(MapperDTO::toReservationEntity).collect(Collectors.toList()));
        user.setDocuments(userDTO.getDocuments().stream().map(MapperDTO::toDocumentEntity).collect(Collectors.toList()));
        /*user.setNotificationsSent(userDTO.getNotificationsSent().stream().map(MapperDTO::toNotificationEntity).collect(Collectors.toList()));
        user.setNotificationsReceived(userDTO.getNotificationsReceived().stream().map(MapperDTO::toNotificationEntity).collect(Collectors.toList()));*/

        return user;
    }

    // Méthodes de transformation pour Document
    public static DocumentDTO toDocumentDTO(Document document) {
        DocumentDTO documentDTO = new DocumentDTO();
        documentDTO.setId(document.getId());
        documentDTO.setUserId(document.getUser().getId());
        documentDTO.setDocumentType(document.getDocumentType());
        documentDTO.setUrl(document.getUrl());
        return documentDTO;
    }

    public static Document toDocumentEntity(DocumentDTO documentDTO) {
        Document document = new Document();
        document.setId(documentDTO.getId());
        User user = new User();
        user.setId(documentDTO.getUserId());
        document.setUser(user);
        document.setDocumentType(documentDTO.getDocumentType());
        document.setUrl(documentDTO.getUrl());
        return document;
    }

    // Méthodes de transformation pour Role
    public static RoleDTO toRoleDTO(Role role) {
        RoleDTO roleDTO = new RoleDTO();
        roleDTO.setId(role.getId());
        roleDTO.setRole(role.getRole());
        return roleDTO;
    }

    public static Role toRoleEntity(RoleDTO roleDTO) {
        Role role = new Role();
        role.setId(roleDTO.getId());
        role.setRole(roleDTO.getRole());
        return role;
    }

    // Ajout des méthodes de conversion pour Evaluation
    public static EvaluationDTO toEvaluationDTO(Evaluation evaluation) {
        if (evaluation == null) {
            return null;
        }
        EvaluationDTO evaluationDTO = new EvaluationDTO();
        evaluationDTO.setId(evaluation.getId());
        evaluationDTO.setNote(evaluation.getNote());
        evaluationDTO.setAvis(evaluation.getAvis());
        evaluationDTO.setReservationId(evaluation.getReservation().getId());
        evaluationDTO.setCreatedAt(evaluation.getCreatedAt());
        evaluationDTO.setUserNom(evaluation.getReservation().getUser().getNom());
        evaluationDTO.setCarBrand(evaluation.getReservation().getCar().getBrand());
        evaluationDTO.setCarModel(evaluation.getReservation().getCar().getModel());
        evaluationDTO.setCarPhotoUrl(evaluation.getReservation().getCar().getPhotos().get(0).getUrl());
        evaluationDTO.setUserProfilePhotoUrl(evaluation.getReservation().getUser().getPhotoUrl());
        evaluationDTO.setUserPrenom(evaluation.getReservation().getUser().getPrenom());
        return evaluationDTO;
    }

    public static Evaluation toEvaluationEntity(EvaluationDTO evaluationDTO) {
        if (evaluationDTO == null) {
            return null;
        }
        Evaluation evaluation = new Evaluation();
        evaluation.setId(evaluationDTO.getId());
        evaluation.setNote(evaluationDTO.getNote());
        evaluation.setAvis(evaluationDTO.getAvis());
        Reservation reservation = new Reservation();
        reservation.setId(evaluationDTO.getReservationId());
        evaluation.setReservation(reservation);
        return evaluation;
    }

    public static PaymentDTO toPaymentDTO(Payment payment) {
        if (payment == null) {
            return null;
        }

        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setId(payment.getId());
        paymentDTO.setPrixTotal(payment.getPrixTotal());
        paymentDTO.setStatut(payment.getStatut());
        paymentDTO.setPaiementMode(payment.getPaiementMode());
        paymentDTO.setPrixPourDriveShare(payment.getPrixPourDriveShare());
        paymentDTO.setPrixPourUser(payment.getPrixPourUser());
        paymentDTO.setCreatedAt(payment.getCreatedAt());

        // Mapping Refund to RefundDTO
        if (payment.getRefund() != null) {
            paymentDTO.setRefundDTO(toRefundDTO(payment.getRefund()));
        }

        paymentDTO.setReservationId(payment.getReservation().getId());

        return paymentDTO;
    }

    // Méthode de transformation DTO -> Entité pour Payment
    public static Payment toPaymentEntity(PaymentDTO paymentDTO) {
        if (paymentDTO == null) {
            return null;
        }

        Payment payment = new Payment();
        payment.setId(paymentDTO.getId());
        payment.setPrixTotal(paymentDTO.getPrixTotal());
        payment.setStatut(paymentDTO.getStatut());
        payment.setPaiementMode(paymentDTO.getPaiementMode());
        payment.setPrixPourDriveShare(paymentDTO.getPrixPourDriveShare());
        payment.setPrixPourUser(paymentDTO.getPrixPourUser());
        payment.setCreatedAt(paymentDTO.getCreatedAt());

        if (paymentDTO.getRefundDTO() != null) {
            payment.setRefund(toRefundEntity(paymentDTO.getRefundDTO()));
        }

        Reservation reservation = new Reservation();
        reservation.setId(paymentDTO.getReservationId());
        payment.setReservation(reservation);

        return payment;
    }


    // Méthode de transformation Entité -> DTO pour Refund
    public static RefundDTO toRefundDTO(Refund refund) {
        if (refund == null) {
            return null;
        }

        RefundDTO refundDTO = new RefundDTO();
        refundDTO.setId(refund.getId());
        refundDTO.setAmount(refund.getAmount());
        refundDTO.setRefundDate(refund.getRefundDate());
        refundDTO.setRefundPercentage(refund.getRefundPercentage());
        refundDTO.setPaymentId(refund.getPayment().getId());

        return refundDTO;
    }

    // Méthode de transformation DTO -> Entité pour Refund
    public static Refund toRefundEntity(RefundDTO refundDTO) {
        if (refundDTO == null) {
            return null;
        }

        Refund refund = new Refund();
        refund.setId(refundDTO.getId());
        refund.setAmount(refundDTO.getAmount());
        refund.setRefundDate(refundDTO.getRefundDate());
        refund.setRefundPercentage(refundDTO.getRefundPercentage());

        Payment payment = new Payment();
        payment.setId(refundDTO.getPaymentId());
        refund.setPayment(payment);

        return refund;
    }


}
