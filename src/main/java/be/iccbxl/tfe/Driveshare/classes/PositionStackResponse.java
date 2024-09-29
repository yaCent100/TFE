package be.iccbxl.tfe.Driveshare.classes;

import java.util.List;

public class PositionStackResponse {

    private List<GeocodingResult> data;

    public List<GeocodingResult> getData() {
        return data;
    }

    public void setData(List<GeocodingResult> data) {
        this.data = data;
    }

    public static class GeocodingResult {
        private double latitude;
        private double longitude;
        private String label;
        private String country;
        private String region;
        private String locality;

        // Getters and Setters

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

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
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
}
