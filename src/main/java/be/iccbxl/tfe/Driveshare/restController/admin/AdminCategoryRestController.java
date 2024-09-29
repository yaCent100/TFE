package be.iccbxl.tfe.Driveshare.restController.admin;

import be.iccbxl.tfe.Driveshare.DTO.CategoryDTO;
import be.iccbxl.tfe.Driveshare.DTO.MapperDTO;
import be.iccbxl.tfe.Driveshare.model.Category;
import be.iccbxl.tfe.Driveshare.service.serviceImpl.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/categories")
@Tag(name = "Admin Category Management", description = "API pour la gestion des catégories par les administrateurs")
public class AdminCategoryRestController {

    @Autowired
    private CategoryService categoryService;

    @Operation(summary = "Obtenir toutes les catégories", description = "Récupérer la liste de toutes les catégories disponibles.")
    @GetMapping
    public List<CategoryDTO> getAllCategories() {
        // Utiliser le mapper pour convertir chaque entité Category en CategoryDTO
        return categoryService.getAllCategory().stream()
                .map(MapperDTO::toCategoryDTO)
                .collect(Collectors.toList());
    }

    @Operation(summary = "Ajouter une nouvelle catégorie", description = "Ajouter une nouvelle catégorie.")
    @PostMapping
    public Category addCategory(@RequestBody Category category) {
        return categoryService.saveCategory(category);
    }

    @Operation(summary = "Supprimer une catégorie", description = "Supprimer une catégorie par son ID.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        // Appel du service pour supprimer la catégorie par ID
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build(); // Retourne 204 No Content si la suppression réussit
    }

}

