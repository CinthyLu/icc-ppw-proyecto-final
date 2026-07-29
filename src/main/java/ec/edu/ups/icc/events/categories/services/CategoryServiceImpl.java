package ec.edu.ups.icc.events.categories.services;

import ec.edu.ups.icc.events.categories.dtos.CreateCategoryDto;
import ec.edu.ups.icc.events.categories.dtos.CategoryResponseDto;
import ec.edu.ups.icc.events.categories.entities.CategoryEntity;
import ec.edu.ups.icc.events.categories.mappers.CategoryMapper;
import ec.edu.ups.icc.events.categories.repositories.CategoryRepository;
import ec.edu.ups.icc.events.core.exceptions.BadRequestException;
import ec.edu.ups.icc.events.core.exceptions.BusinessRuleException;
import ec.edu.ups.icc.events.core.exceptions.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryServiceImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public List<CategoryResponseDto> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(CategoryMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryResponseDto getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .map(CategoryMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con id: " + id));
    }

    @Override
    public CategoryResponseDto createCategory(CreateCategoryDto dto) {
        if (dto.getName() == null || dto.getName().isBlank()) {
            throw new BadRequestException("El nombre de la categoría es obligatorio");
        }

        String normalizedName = dto.getName().trim().toLowerCase(Locale.ROOT);
        if (categoryRepository.existsByNameIgnoreCase(normalizedName)) {
            throw new BusinessRuleException("La categoría ya existe");
        }

        CategoryEntity category = CategoryMapper.toEntity(dto);
        category.setName(normalizedName);
        return CategoryMapper.toResponse(categoryRepository.save(category));
    }

    @Override
    public CategoryResponseDto updateCategory(Long id, CreateCategoryDto dto) {
        CategoryEntity category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con id: " + id));

        if (dto.getName() != null && !dto.getName().isBlank()) {
            String normalizedName = dto.getName().trim().toLowerCase(Locale.ROOT);
            if (categoryRepository.existsByNameIgnoreCaseAndIdNot(normalizedName, id)) {
                throw new BusinessRuleException("La categoría ya existe");
            }
            category.setName(normalizedName);
        }
        category.setDescription(dto.getDescription());
        return CategoryMapper.toResponse(categoryRepository.save(category));
    }

    @Override
    public void deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Categoría no encontrada con id: " + id);
        }
        categoryRepository.deleteById(id);
    }
}
