package be.iccbxl.tfe.Driveshare.service.serviceImpl;

import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.model.GeocodingResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GeocodingService {


    private final GeoApiContext geoApiContext;
    private final Logger logger = LoggerFactory.getLogger(GeocodingService.class);
    private final Map<String, GeocodingResult> geocodeCache = new ConcurrentHashMap<>();

    public GeocodingService(@Value("${google.maps.api.key}") String apiKey) {
        this.geoApiContext = new GeoApiContext.Builder()
                .apiKey(apiKey)
                .build();
    }

    public GeocodingResult geocode(String address) {
        return geocodeCache.computeIfAbsent(address, this::callGoogleMapsApi);
    }

    private GeocodingResult callGoogleMapsApi(String address) {
        try {
            GeocodingResult[] results = GeocodingApi.geocode(geoApiContext, address).await();
            if (results != null && results.length > 0) {
                return results[0];
            }
        } catch (Exception e) {
            logger.error("Geocoding API request failed for address {}: {}", address, e.getMessage());
        }
        return null;
    }
}