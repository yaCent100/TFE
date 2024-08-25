package be.iccbxl.tfe.Driveshare.restController.admin;

import be.iccbxl.tfe.Driveshare.DTO.FeatureDTO;
import be.iccbxl.tfe.Driveshare.DTO.MapperDTO;
import be.iccbxl.tfe.Driveshare.model.Feature;
import be.iccbxl.tfe.Driveshare.service.serviceImpl.FeatureService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RequestMapping("/api/admin/features")
@RestController
public class AdminFeaturesRestController {

    @Autowired
    private FeatureService featureService;

    @Operation(summary = "Obtenir toutes les fonctionnalités", description = "Récupérer la liste de toutes les fonctionnalités disponibles.")
    @GetMapping
    public List<FeatureDTO> getAllFeatures() {
        return featureService.getAllFeatures().stream()
                .map(MapperDTO::toFeatureDTO)
                .collect(Collectors.toList());
    }

    @PostMapping
    public Feature addFeature(@RequestBody Feature feature) {
        return featureService.saveFeature(feature);
    }
}
