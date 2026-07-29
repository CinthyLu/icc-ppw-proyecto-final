package ec.edu.ups.icc.events.events.dtos;

import ec.edu.ups.icc.events.events.entities.EventModality;
import ec.edu.ups.icc.events.events.entities.EventStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "DTO para actualizar un evento académico existente")
public class UpdateEventDto {

    @Schema(description = "Título actualizado del evento", example = "Congreso de Ciencias de la Computación 2026", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "El título es obligatorio")
    @Size(min = 3, max = 200, message = "El título debe tener entre 3 y 200 caracteres")
    private String title;

    @Schema(description = "Descripción actualizada del evento", example = "Un espacio de divulgación y conferencias sobre avances tecnológicos.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "La descripción es obligatoria")
    private String description;

    @Schema(description = "Modalidad del evento (PRESENTIAL, VIRTUAL, HYBRID)", example = "PRESENTIAL", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "La modalidad es obligatoria")
    private EventModality modality;

    @Schema(description = "Lugar o dirección física", example = "Auditorio Generalups, Campus Girón")
    private String location;

    @Schema(description = "Capacidad máxima de asistentes", example = "150", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "La capacidad es obligatoria")
    @Min(value = 1, message = "La capacidad debe ser al menos de 1 participante")
    private Integer capacity;

    @Schema(description = "Fecha y hora de inicio del evento", example = "2026-09-20T08:00:00Z", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "La fecha de inicio es obligatoria")
    private LocalDateTime startDate;

    @Schema(description = "Fecha y hora de finalización del evento", example = "2026-09-22T17:00:00Z", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "La fecha de finalización es obligatoria")
    private LocalDateTime endDate;

    @Schema(description = "ID de la categoría del evento", example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "La categoría es obligatoria")
    private Long categoryId;

    @Schema(description = "Estado del evento (DRAFT, PUBLISHED, CANCELLED)", example = "PUBLISHED", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "El estado del evento es obligatorio")
    private EventStatus status;

    public UpdateEventDto() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public EventModality getModality() {
        return modality;
    }

    public void setModality(EventModality modality) {
        this.modality = modality;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public EventStatus getStatus() {
        return status;
    }

    public void setStatus(EventStatus status) {
        this.status = status;
    }
}
