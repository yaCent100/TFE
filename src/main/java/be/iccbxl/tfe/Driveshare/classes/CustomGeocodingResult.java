package be.iccbxl.tfe.Driveshare.classes;

import lombok.Data;

@Data
public class CustomGeocodingResult {
    private String placeName;
    private double latitude;
    private double longitude;
    private String country;
    private String region;
    private String locality;

    public CustomGeocodingResult(String placeName, double latitude, double longitude, String country, String region, String locality) {
        this.placeName = placeName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.country = country;
        this.region = region;
        this.locality = locality;
    }

    // Getters and setters
    public String getPlaceName() {
        return placeName;
    }

    public void setPlaceName(String placeName) {
        this.placeName = placeName;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getLocality() {
        return locality;
    }

    public void setLocality(String locality) {
        this.locality = locality;
    }
}
