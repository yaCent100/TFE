package be.iccbxl.tfe.Driveshare.service.serviceImpl;

import be.iccbxl.tfe.Driveshare.model.Equipment;
import be.iccbxl.tfe.Driveshare.repository.EquipmentRepository;
import be.iccbxl.tfe.Driveshare.service.EquipmentServiceI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
}
