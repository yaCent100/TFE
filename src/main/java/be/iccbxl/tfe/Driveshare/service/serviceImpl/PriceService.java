package be.iccbxl.tfe.Driveshare.service.serviceImpl;

import be.iccbxl.tfe.Driveshare.model.Category;
import be.iccbxl.tfe.Driveshare.model.Price;
import be.iccbxl.tfe.Driveshare.repository.PriceRepository;
import be.iccbxl.tfe.Driveshare.service.PriceServiceI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class PriceService implements PriceServiceI {

    @Autowired
    private PriceRepository priceRepository;


    @Override
    public double calculatePrice(Category cat, LocalDate date) {
        boolean isHighSeason = isHighSeason(date);
        Price price = priceRepository.findByCategoryAndSeason(cat, isHighSeason ? "High" : "Low");
        if (price != null) {
            return price.getPrice();
        }
        return findBasePrice(cat); // Fallback price
    }

    @Override
    public boolean isHighSeason(LocalDate date) {
        return (date.getMonthValue() >= 6 && date.getMonthValue() <= 8);
    }

    @Override
    public boolean isLowSeason(LocalDate date) {
        return !isHighSeason(date);
    }

    private double findBasePrice(Category cat) {
        return 100.00;
    }
}
