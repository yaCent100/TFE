package be.iccbxl.tfe.Driveshare.service.serviceImpl;

import be.iccbxl.tfe.Driveshare.model.Price;
import be.iccbxl.tfe.Driveshare.service.PriceServiceI;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class PriceService implements PriceServiceI {

    // Méthode pour calculer le prix affiché
    public double calculateDisplayPrice(Price price, LocalDate date) {
        if (price == null) {
            throw new IllegalArgumentException("Price information is missing.");
        }

        // Vérifier que la date n'est pas nulle
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null.");
        }

        double basePrice = price.getMiddlePrice(); // Exemple d'utilisation d'un prix de base
        double displayPrice = basePrice;

        // Ajuster le prix selon la saison
        if (isHighSeason(date)) {
            displayPrice = price.getHighPrice();
        } else if (isLowSeason(date)) {
            displayPrice = price.getLowPrice();
        }

        // Appliquer la promotion si nécessaire
        displayPrice = applyPromotion(displayPrice, price);

        return displayPrice;
    }

    // Méthode pour vérifier si c'est la haute saison
    private boolean isHighSeason(LocalDate date) {
        // Implémentez votre logique pour déterminer si c'est la haute saison
        // Exemple: haute saison de juin à août
        return (date.getMonthValue() >= 6 && date.getMonthValue() <= 8);
    }

    // Méthode pour vérifier si c'est la basse saison
    private boolean isLowSeason(LocalDate date) {
        // Implémentez votre logique pour déterminer si c'est la basse saison
        // Exemple: basse saison de décembre à février
        return (date.getMonthValue() == 12 || date.getMonthValue() == 1 || date.getMonthValue() == 2);
    }

    // Méthode pour appliquer les promotions
    private double applyPromotion(double price, Price priceEntity) {
        if (priceEntity == null) {
            throw new IllegalArgumentException("Price entity cannot be null for applying promotions.");
        }

        double promo1 = priceEntity.getPromo1() / 100.0;
        return price * (1 - promo1);
    }
}
