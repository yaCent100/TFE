package be.iccbxl.tfe.Driveshare.service;

import be.iccbxl.tfe.Driveshare.model.Car;
import be.iccbxl.tfe.Driveshare.model.Category;

import java.util.List;

public interface CategoryServiceI {

    List<Category> getAllCategory();
    Category getCategoryById(Long id);
    Category saveCategory(Category cat);
    Category updateCategory(Long id, Category cat);
    void deleteCategory(Long id);
}
