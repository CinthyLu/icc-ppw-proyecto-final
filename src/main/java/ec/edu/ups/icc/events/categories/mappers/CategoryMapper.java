package ec.edu.ups.icc.events.categories.mappers;

import ec.edu.ups.icc.events.categories.dtos.CreateCategoryDto;
import ec.edu.ups.icc.events.categories.dtos.CategoryResponseDto;
import ec.edu.ups.icc.events.categories.entities.CategoryEntity;

public class CategoryMapper {

    public static CategoryEntity toEntity(CreateCategoryDto dto) {
        if (dto == null) {
            return null;
        }
        CategoryEntity entity = new CategoryEntity();
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        return entity;
    }

    public static CategoryResponseDto toResponse(CategoryEntity entity) {
        if (entity == null) {
            return null;
        }
        CategoryResponseDto dto = new CategoryResponseDto();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());
        return dto;
    }
}
