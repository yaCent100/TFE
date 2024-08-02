package be.iccbxl.tfe.Driveshare.DTO;

import be.iccbxl.tfe.Driveshare.model.*;
import jakarta.persistence.EntityNotFoundException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class CarMapper {

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
            carDTO.setLatitude(car.getLatitude());
            carDTO.setLongitude(car.getLongitude());
            carDTO.setPhotoUrl(car.getPhotos().stream().map(Photo::getUrl).collect(Collectors.toList())); // Convertir les photos en URL
            carDTO.setPrice(CarMapper.toPriceDTO(car.getPrice())); // Convertir Price en PriceDTO
            carDTO.setReservationDTOS(car.getReservations().stream().map(CarMapper::toReservationDTO).collect(Collectors.toList()));
            carDTO.setUnavailables(car.getUnavailable().stream().map(CarMapper::toIndisponibleDTO).collect(Collectors.toList()));


            // Conversion de la liste des features
            List<FeatureDTO> featureDTOs = car.getFeatures().stream()
                    .map(feature -> new FeatureDTO(feature.getId(), feature.getDescription()))
                    .collect(Collectors.toList());
            carDTO.setFeatures(featureDTOs);

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
        car.setReservations(carDTO.getReservationDTOS().stream().map(CarMapper::toReservationEntity).collect(Collectors.toList()));
        car.setUnavailable(carDTO.getUnavailables().stream().map(CarMapper::toIndisponibleEntity).collect(Collectors.toList()));
        return car;
    }

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


            String photoUrl = reservation.getUser().getPhotoUrl();
            if (photoUrl != null && !photoUrl.isEmpty()) {
                reservationDTO.setUserProfileImage("/uploads/profil/" + photoUrl);
            } else {
                reservationDTO.setUserProfileImage("/uploads/profil/defaultPhoto.png");
            }

            // Obtenez l'URL de la première photo de la liste des photos de la voiture
            if (!reservation.getCar().getPhotos().isEmpty()) {
                reservationDTO.setCarImage("/uploads/"+reservation.getCar().getPhotos().get(0).getUrl());
            } else {
                reservationDTO.setCarImage("images/carDefault.png"); // URL par défaut si aucune photo n'est disponible
            }
            return reservationDTO;
        }

    public static Reservation toReservationEntity(ReservationDTO reservationDTO) {
        if (reservationDTO == null) {
            return null;
        }
        Reservation reservation = new Reservation();
        reservation.setId(reservationDTO.getId());
        reservation.setCar(reservationDTO.getCar());
        // Créez une instance de CarRental si nécessaire et définissez-la sur la réservation

        reservation.setDebutLocation(LocalDate.parse(reservationDTO.getDebutLocation()));
        reservation.setFinLocation(LocalDate.parse(reservationDTO.getFinLocation()));
        reservation.setStatut(reservationDTO.getStatut());

        return reservation;
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

    private static CarDTO convertToCarDTO(Car car) {
        List<FeatureDTO> featureDTOs = car.getFeatures().stream()
                .map(feature -> new FeatureDTO(feature.getId(), feature.getName()))
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
        priceDTO.setHighPrice(price.getMiddlePrice());
        priceDTO.setPromo1(price.getPromo1());
        priceDTO.setPromo2(price.getPromo1());
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

    public static ChatMessageDTO toChatMessageDTO(ChatMessage chatMessage) {
        ChatMessageDTO chatMessageDTO = new ChatMessageDTO();
        chatMessageDTO.setContent(chatMessage.getContent());
        chatMessageDTO.setFromUserNom(chatMessage.getReservation().getUser().getNom()); // Assurez-vous d'avoir une méthode getNom() dans la classe User
        chatMessageDTO.setFromUserId(chatMessage.getReservation().getUser().getId());
        chatMessageDTO.setToUserId(chatMessage.getReservation().getCar().getUser().getId());
        chatMessageDTO.setReservationId(chatMessage.getReservation().getId());
        return chatMessageDTO;
    }
    public static Notification toNotification(NotificationDTO dto, User fromUser, User toUser, Car car) {
        Notification notification = new Notification();
        notification.setMessage(dto.getMessage());
        notification.setFromUser(fromUser);
        notification.setToUser(toUser);
        notification.setCar(car);
        notification.setType(dto.getType()); // Assurez-vous que le type est défini correctement
        notification.setDateEnvoi(LocalDateTime.now());
        return notification;
    }

    public static NotificationDTO toDTO(Notification notification) {
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
        userDTO.setOwnedCars(user.getOwnedCars().stream().map(car -> {
            CarDTO carDTO = CarMapper.convertToCarDTO(car);
            carDTO.setUser(null); // Eviter la boucle infinie
            return carDTO;
        }).collect(Collectors.toList()));
        return userDTO;
    }






}

