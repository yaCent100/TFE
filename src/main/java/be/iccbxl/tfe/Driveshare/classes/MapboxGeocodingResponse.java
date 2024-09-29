package be.iccbxl.tfe.Driveshare.classes;
import java.util.List;

public class MapboxGeocodingResponse {

    private List<Feature> features;

    public List<Feature> getFeatures() {
        return features;
    }

    public void setFeatures(List<Feature> features) {
        this.features = features;
    }

    public static class Feature {
        private String place_name;
        private List<Double> center;
        private List<Context> context; // Modifier ici pour en faire une liste

        public String getPlaceName() {
            return place_name;
        }

        public void setPlaceName(String place_name) {
            this.place_name = place_name;
        }

        public List<Double> getCenter() {
            return center;
        }

        public void setCenter(List<Double> center) {
            this.center = center;
        }

        public List<Context> getContext() {
            return context;
        }

        public void setContext(List<Context> context) {
            this.context = context;
        }
    }

    public static class Context {
        private String id;
        private String text;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }
    }
}
