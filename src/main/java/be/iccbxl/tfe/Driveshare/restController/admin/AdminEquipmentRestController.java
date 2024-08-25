package be.iccbxl.tfe.Driveshare.restController.admin;

import be.iccbxl.tfe.Driveshare.DTO.EquipmentDTO;
import be.iccbxl.tfe.Driveshare.DTO.MapperDTO;
import be.iccbxl.tfe.Driveshare.model.Equipment;
import be.iccbxl.tfe.Driveshare.service.serviceImpl.EquipmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/equipments")
@Tag(name = "Admin Equipment Management", description = "API pour la gestion des équipements par les administrateurs")
public class AdminEquipmentRestController {

    @Autowired
    private EquipmentService equipmentService;

    @Operation(summary = "Obtenir tous les équipements", description = "Récupérer la liste de tous les équipements disponibles.")
    @GetMapping
    public List<EquipmentDTO> getAllEquipments() {
        return equipmentService.getAllEquipments().stream()
                .map(MapperDTO::toEquipmentDTO)
                .collect(Collectors.toList());
    }

    @Operation(summary = "Ajouter un nouvel équipement", description = "Ajouter un nouvel équipement avec une icône.")
    @PostMapping
    public Equipment addEquipment(@RequestPart("equipment") Equipment equipment,
                                  @RequestPart("icon") MultipartFile iconFile) throws IOException {
        return equipmentService.addEquipment(equipment, iconFile);
    }
}

