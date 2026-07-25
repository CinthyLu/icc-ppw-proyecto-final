package ec.edu.ups.icc.events.categories.services;

import ec.edu.ups.icc.events.categories.dtos.CategoryDTO;
import ec.edu.ups.icc.events.categories.entities.CategoryEntity;
import ec.edu.ups.icc.events.categories.repositories.CategoryRepository;
import ec.edu.ups.icc.events.core.exceptions.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<CategoryDTO> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public CategoryDTO getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con id: " + id));
    }

    public CategoryDTO createCategory(CategoryDTO dto) {
        if (dto.name() == null || dto.name().isBlank()) {
            throw new IllegalArgumentException("El nombre de la categoría es obligatorio");
        }
        if (categoryRepository.existsByNameIgnoreCase(dto.name())) {
            throw new IllegalArgumentException("La categoría ya existe");
        }
        CategoryEntity category = new CategoryEntity();
        category.setName(dto.name());
        category.setDescription(dto.description());
        return toDto(categoryRepository.save(category));
    }

    public CategoryDTO updateCategory(Long id, CategoryDTO dto) {
        CategoryEntity category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con id: " + id));

        if (dto.name() != null && !dto.name().isBlank()) {
            category.setName(dto.name());
        }
        category.setDescription(dto.description());
        return toDto(categoryRepository.save(category));
    }

    public void deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Categoría no encontrada con id: " + id);
        }
        categoryRepository.deleteById(id);
    }

    private CategoryDTO toDto(CategoryEntity entity) {
        return new CategoryDTO(entity.getId(), entity.getName(), entity.getDescription());
    }
}
