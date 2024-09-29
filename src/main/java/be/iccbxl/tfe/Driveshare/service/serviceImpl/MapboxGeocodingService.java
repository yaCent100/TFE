package be.iccbxl.tfe.Driveshare.service.serviceImpl;

import be.iccbxl.tfe.Driveshare.classes.CustomGeocodingResult;
import be.iccbxl.tfe.Driveshare.classes.MapboxGeocodingResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;

@Service
public class MapboxGeocodingService {

    private final Logger logger = LoggerFactory.getLogger(MapboxGeocodingService.class);
    private final RestTemplate restTemplate;
    private final String accessToken;
    private final String geocodingUrl = "https://api.mapbox.com/geocoding/v5/mapbox.places/";

    public MapboxGeocodingService(@Value("${mapbox.api.key}") String accessToken) {
        this.restTemplate = new RestTemplate();
        this.accessToken = accessToken;
    }

    @Cacheable(value = "geocodingCache", key = "#fullAddress")
    public CustomGeocodingResult geocodeAddress(String fullAddress) throws Exception {
        String url = geocodingUrl + encodeURIComponent(fullAddress) + ".json?access_token=" + accessToken;

        try {
            MapboxGeocodingResponse response = restTemplate.getForObject(url, MapboxGeocodingResponse.class);

            if (response != null && response.getFeatures() != null && !response.getFeatures().isEmpty()) {
                MapboxGeocodingResponse.Feature feature = response.getFeatures().get(0);
                List<Double> coordinates = feature.getCenter();

                if (coordinates != null && coordinates.size() == 2) {
                    return new CustomGeocodingResult(
                            feature.getPlaceName(),
                            coordinates.get(1), // latitude
                            coordinates.get(0), // longitude
                            getContextText(feature, "country"),
                            getContextText(feature, "region"),
                            getContextText(feature, "locality")
                    );
                }
            }
        } catch (Exception e) {
            logger.error("Error while geocoding address: {}", fullAddress, e);
            throw new Exception("Error while geocoding address: " + fullAddress);
        }

        throw new Exception("No geocoding result found for address: " + fullAddress);
    }

    private String getContextText(MapboxGeocodingResponse.Feature feature, String type) {
        if (feature.getContext() != null) {
            for (MapboxGeocodingResponse.Context context : feature.getContext()) {
                if (context.getId().contains(type)) {
                    return context.getText();
                }
            }
        }
        return null;
    }

    private String encodeURIComponent(String value) {
        try {
            return java.net.URLEncoder.encode(value, java.nio.charset.StandardCharsets.UTF_8.toString());
        } catch (Exception e) {
            logger.error("Error encoding URL component: {}", value, e);
            return value; // fallback to original value if encoding fails
        }
    }
}
