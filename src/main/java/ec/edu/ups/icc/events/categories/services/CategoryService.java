package ec.edu.ups.icc.events.categories.services;


import ec.edu.ups.icc.events.categories.dtos.CategoryResponseDto;
import ec.edu.ups.icc.events.categories.dtos.CreateCategoryDto;


import java.util.List;



public interface CategoryService {
    List<CategoryResponseDto> getAllCategories();
    CategoryResponseDto getCategoryById(Long id);
    CategoryResponseDto createCategory(CreateCategoryDto dto);
    CategoryResponseDto updateCategory(Long id, CreateCategoryDto dto);
    void deleteCategory(Long id);
}
