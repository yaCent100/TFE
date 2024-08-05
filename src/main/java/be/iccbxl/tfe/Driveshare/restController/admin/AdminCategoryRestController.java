package be.iccbxl.tfe.Driveshare.restController.admin;

import be.iccbxl.tfe.Driveshare.DTO.CategoryDTO;
import be.iccbxl.tfe.Driveshare.model.Category;
import be.iccbxl.tfe.Driveshare.service.serviceImpl.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@RequestMapping("/api/admin/categories")
public class AdminCategoryRestController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping
    public List<Category> getAllCategories() {
        return categoryService.getAllCategory();
    }

    @PostMapping
    public Category addCategory(@RequestBody Category category) {
        return categoryService.saveCategory(category);
    }
}
