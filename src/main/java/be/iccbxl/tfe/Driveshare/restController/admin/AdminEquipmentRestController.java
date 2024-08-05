package be.iccbxl.tfe.Driveshare.restController.admin;

import be.iccbxl.tfe.Driveshare.model.Equipment;
import be.iccbxl.tfe.Driveshare.service.serviceImpl.EquipmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/admin/equipments")
public class AdminEquipmentRestController {

    @Autowired
    private EquipmentService equipmentService;

    @GetMapping
    public List<Equipment> getAllEquipments() {
        return equipmentService.getAllEquipments();
    }

    @PostMapping
    public Equipment addEquipment(@RequestPart("equipment") Equipment equipment,
                                     @RequestPart("icon") MultipartFile iconFile) throws IOException {
        return equipmentService.addEquipment(equipment, iconFile);
    }
}
