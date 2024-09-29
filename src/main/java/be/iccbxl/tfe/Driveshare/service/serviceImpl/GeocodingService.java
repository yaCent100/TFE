package be.iccbxl.tfe.Driveshare.service.serviceImpl;

import be.iccbxl.tfe.Driveshare.classes.CustomGeocodingResult;
import be.iccbxl.tfe.Driveshare.classes.PositionStackResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;

@Service
public class GeocodingService {

    private final Logger logger = LoggerFactory.getLogger(GeocodingService.class);
    private final RestTemplate restTemplate;
    private final String apiKey;
    private final String geocodingUrl;

    public GeocodingService(@Value("${positionstack.api.key}") String apiKey) {
        this.apiKey = apiKey;
        this.restTemplate = new RestTemplate();
        this.geocodingUrl = "http://api.positionstack.com/v1/forward?access_key=" + apiKey + "&query=";
    }

    @Cacheable(value = "geocodingCache", key = "#fullAddress", unless = "#result == null")
    public CustomGeocodingResult geocodeAddress(String fullAddress) {
        logger.info("Full address to geocode: {}", fullAddress);
        return geocode(fullAddress);
    }

    private CustomGeocodingResult geocode(String fullAddress) {
        if (fullAddress == null || fullAddress.trim().isEmpty()) {
            logger.warn("Adresse vide ou nulle fournie pour le géocodage.");
            return null;
        }

        try {
            String url = geocodingUrl + fullAddress;
            PositionStackResponse response = restTemplate.getForObject(url, PositionStackResponse.class);

            if (response != null && response.getData() != null && !response.getData().isEmpty()) {
                PositionStackResponse.GeocodingResult result = response.getData().get(0);
                CustomGeocodingResult geocodingResult = new CustomGeocodingResult(
                        result.getLabel(),
                        result.getLatitude(),
                        result.getLongitude(),
                        result.getCountry(),
                        result.getRegion(),
                        result.getLocality()
                );

                return geocodingResult;
            } else {
                logger.warn("Aucun résultat de géocodage pour l'adresse : {}", fullAddress);
            }
        } catch (Exception e) {
            logger.error("Erreur lors de la requête à l'API de géocodage pour l'adresse {}: {}", fullAddress, e.getMessage());
        }

        return null;
    }

}
