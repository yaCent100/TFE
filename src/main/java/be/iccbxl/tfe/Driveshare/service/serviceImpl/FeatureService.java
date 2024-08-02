package be.iccbxl.tfe.Driveshare.service.serviceImpl;

import be.iccbxl.tfe.Driveshare.model.Feature;
import be.iccbxl.tfe.Driveshare.repository.FeatureRepository;
import be.iccbxl.tfe.Driveshare.service.FeatureServiceI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class FeatureService implements FeatureServiceI {

    @Autowired
    private FeatureRepository featureRepository;

    @Override
    public List<Feature> getAllFeatures() {
        List<Feature> features = new ArrayList<>();
        featureRepository.findAll().forEach(features::add);
        return features;      }

    @Override
    public Feature getFeatureById(Long id) {
        Optional<Feature> optionalFeature = featureRepository.findById(id);
        return optionalFeature.orElse(null);
    }

    @Override
    public Feature saveFeature(Feature feature) {
        return featureRepository.save(feature);
    }

    @Override
    public Feature updateFeature(Long id, Feature newFeature) {
        Optional<Feature> optionalFeature = featureRepository.findById(id);
        if (optionalFeature.isPresent()) {
            Feature existingFeature = optionalFeature.get();
            existingFeature.setName(newFeature.getName());
            existingFeature.setDescription(newFeature.getDescription());
            return featureRepository.save(existingFeature);
        }
        return null;
    }

    @Override
    public void deleteFeature(Long id) {
        featureRepository.deleteById(id);
    }

    @Override
    public List<Feature> findByCategory(String name) {
        return featureRepository.findByName(name);
    }

    public void saveFeature(Feature featureName, String featureDescription) {
    }
}
