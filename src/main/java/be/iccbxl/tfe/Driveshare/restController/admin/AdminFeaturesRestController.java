package be.iccbxl.tfe.Driveshare.restController.admin;

import be.iccbxl.tfe.Driveshare.DTO.FeatureDTO;
import be.iccbxl.tfe.Driveshare.model.Feature;
import be.iccbxl.tfe.Driveshare.service.serviceImpl.FeatureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

public class AdminFeaturesRestController {

    @Autowired
    private FeatureService featureService;

    @GetMapping
    public List<Feature> getAllFeatures() {
        return featureService.getAllFeatures();
    }

    @PostMapping
    public Feature addFeature(@RequestBody Feature feature) {
        return featureService.saveFeature(feature);
    }
}
