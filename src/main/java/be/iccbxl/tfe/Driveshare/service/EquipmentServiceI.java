package be.iccbxl.tfe.Driveshare.service;

import be.iccbxl.tfe.Driveshare.model.Condition;
import be.iccbxl.tfe.Driveshare.model.Equipment;

import java.util.List;

public interface EquipmentServiceI {

    List<Equipment> getAllEquipments();
    Equipment getEquipmentById(Long id);
    Equipment saveEquipment(Equipment equipment);
    Equipment updateEquipment(Long id, Equipment equipment);
    void deleteEquipment(Long id);
}
