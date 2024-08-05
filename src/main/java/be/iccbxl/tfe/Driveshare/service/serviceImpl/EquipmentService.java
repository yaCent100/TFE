package be.iccbxl.tfe.Driveshare.service.serviceImpl;

import be.iccbxl.tfe.Driveshare.model.Equipment;
import be.iccbxl.tfe.Driveshare.repository.EquipmentRepository;
import be.iccbxl.tfe.Driveshare.service.EquipmentServiceI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class EquipmentService implements EquipmentServiceI {

    @Autowired
    private EquipmentRepository equipmentRepository;

    @Override
    public List<Equipment> getAllEquipments() {
        List<Equipment> equipments = new ArrayList<>();
        equipmentRepository.findAll().forEach(equipments::add);
        return equipments;       }

    @Override
    public Equipment getEquipmentById(Long id) {
        Optional<Equipment> optionalEquipment = equipmentRepository.findById(id);
        return optionalEquipment.orElse(null);
    }
    @Override
    public List<Equipment> getEquipmentByIds(List<Long> ids) {
        return (List<Equipment>) equipmentRepository.findAllById(ids);
    }

    @Override
    public Equipment saveEquipment(Equipment equipment) {
        return equipmentRepository.save(equipment);
    }

    @Override
    public Equipment updateEquipment(Long id, Equipment newEquipment) {
        Optional<Equipment> optionalEquipment = equipmentRepository.findById(id);
        if (optionalEquipment.isPresent()) {
            Equipment existingEquipment = optionalEquipment.get();
            existingEquipment.setEquipment(newEquipment.getEquipment());
            return equipmentRepository.save(existingEquipment);
        }
        return null;
    }

    @Override
    public void deleteEquipment(Long id) {
        equipmentRepository.deleteById(id);
    }

    public void saveEquipment(Equipment equipmentName, String icone) {
    }

    public Equipment addEquipment(Equipment equipmentDTO, MultipartFile iconFile) throws IOException {
        String fileName = saveFile(iconFile);

        Equipment equipment = new Equipment();
        equipment.setEquipment(equipmentDTO.getEquipment());
        equipment.setIcone(fileName);  // Save the file path relative to the upload directory

        return equipmentRepository.save(equipment);

    }

    private String saveFile(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return "icones/default.png";
        }

        String directory = "icones/";
        Path uploadPath = Paths.get(directory);

        // Create directories if they don't exist
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String fileName = directory + file.getOriginalFilename();
        Path path = Paths.get(fileName);
        Files.write(path, file.getBytes());

        return fileName;
    }
}
