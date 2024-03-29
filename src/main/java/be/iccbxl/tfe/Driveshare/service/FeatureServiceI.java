package be.iccbxl.tfe.Driveshare.service;

import be.iccbxl.tfe.Driveshare.model.Evaluation;
import be.iccbxl.tfe.Driveshare.model.Feature;

import java.util.List;

public interface FeatureServiceI {

    List<Feature> getAllFeatures();
    Feature getFeatureById(Long id);
    Feature saveFeature(Feature feature);
    Feature updateFeature(Long id, Feature feature);
    void deleteFeature(Long id);
}
