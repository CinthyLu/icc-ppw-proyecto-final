package ec.edu.ups.icc.events.categories.dtos;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Respuesta de detalles de una categoría")
public class CategoryResponseDto {

    @Schema(description = "ID único de la categoría", example = "1")
    private Long id;

    @Schema(description = "Nombre de la categoría", example = "Tecnología y Software")
    private String name;

    @Schema(description = "Descripción de la categoría", example = "Eventos y conferencias sobre desarrollo de software y tecnología")
    private String description;

    public CategoryResponseDto() {
    }

    public CategoryResponseDto(Long id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
