package ec.edu.ups.icc.events.categories.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "DTO para crear una categoría de eventos")
public class CreateCategoryDto {

    @Schema(description = "Nombre de la categoría", example = "Ciencia y Medicina", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
    private String name;

    @Schema(description = "Descripción detallada de la categoría", example = "Eventos académicos sobre ramas científicas y médicas")
    private String description;

    public CreateCategoryDto() {
    }

    public CreateCategoryDto(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
