package be.iccbxl.tfe.Driveshare.DTO;

import be.iccbxl.tfe.Driveshare.model.*;

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
            carDTO.setModel(car.getModel());
            carDTO.setAdresse(car.getAdresse());
            carDTO.setLatitude(car.getLatitude());
            carDTO.setLongitude(car.getLongitude());
            carDTO.setPhotoUrl(car.getPhotos().stream().map(Photo::getUrl).collect(Collectors.toList())); // Convertir les photos en URL
            carDTO.setPrice(CarMapper.toPriceDTO(car.getPrice())); // Convertir Price en PriceDTO
            carDTO.setCarRentals(car.getCarRentals().stream().map(CarMapper::toCarRentalDTO).collect(Collectors.toList()));
            carDTO.setUnavailables(car.getUnavailable().stream().map(CarMapper::toIndisponibleDTO).collect(Collectors.toList()));
            return carDTO;
        }

        public static CarRentalDTO toCarRentalDTO(CarRental carRental) {
            CarRentalDTO carRentalDTO = new CarRentalDTO();
            carRentalDTO.setId(carRental.getId());
            carRentalDTO.setReservations(carRental.getReservations().stream().map(CarMapper::toReservationDTO).collect(Collectors.toList()));
            return carRentalDTO;
        }

        public static ReservationDTO toReservationDTO(Reservation reservation) {
            ReservationDTO reservationDTO = new ReservationDTO();
            reservationDTO.setId(reservation.getId());
            reservationDTO.setCarRentalId(reservation.getCarRental().getId());
            reservationDTO.setCarId(reservation.getCarRental().getCar().getId());
            reservationDTO.setDebutLocation(reservation.getDebutLocation().toString());
            reservationDTO.setFinLocation(reservation.getFinLocation().toString());
            reservationDTO.setStatut(reservation.getStatut());
            reservationDTO.setCarBrand(reservation.getCarRental().getCar().getBrand());
            reservationDTO.setCarModel(reservation.getCarRental().getCar().getModel());
            reservationDTO.setUserName(reservation.getCarRental().getUser().getPrenom() + " " + reservation.getCarRental().getUser().getNom());


            String photoUrl = reservation.getCarRental().getUser().getPhotoUrl();
            if (photoUrl != null && !photoUrl.isEmpty()) {
                reservationDTO.setUserProfileImage("/uploads/profil/" + photoUrl);
            } else {
                reservationDTO.setUserProfileImage("/uploads/profil/defaultPhoto.png");
            }

            // Obtenez l'URL de la première photo de la liste des photos de la voiture
            if (!reservation.getCarRental().getCar().getPhotos().isEmpty()) {
                reservationDTO.setCarImage("/uploads/"+reservation.getCarRental().getCar().getPhotos().get(0).getUrl());
            } else {
                reservationDTO.setCarImage("images/carDefault.png"); // URL par défaut si aucune photo n'est disponible
            }
            return reservationDTO;
        }



        public static IndisponibleDTO toIndisponibleDTO(Indisponible indisponible) {
            IndisponibleDTO indisponibleDTO = new IndisponibleDTO();
            indisponibleDTO.setId(indisponible.getId());
            indisponibleDTO.setStartDate(indisponible.getStartDate());
            indisponibleDTO.setEndDate(indisponible.getEndDate());
            return indisponibleDTO;
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

    public static ChatMessage toChatMessage(ChatMessageDTO chatMessageDTO) {
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setContent(chatMessageDTO.getContent());
        chatMessage.setReservationId(chatMessageDTO.getReservationId());

        return chatMessage;
    }

    public static ChatMessageDTO toChatMessageDTO(ChatMessage chatMessage) {
        ChatMessageDTO chatMessageDTO = new ChatMessageDTO();
        chatMessageDTO.setContent(chatMessage.getContent());
        chatMessageDTO.setFromUserNom(chatMessage.getFromUser().getNom()); // Assurez-vous d'avoir une méthode getNom() dans la classe User
        chatMessageDTO.setFromUserId(chatMessage.getFromUser().getId());
        chatMessageDTO.setToUserId(chatMessage.getToUser().getId());
        chatMessageDTO.setReservationId(chatMessage.getReservationId());
        return chatMessageDTO;
    }



}

