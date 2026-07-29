package ec.edu.ups.icc.events.sessions.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "DTO para crear una nueva sesión o conferencia dentro de un evento")
public class CreateSessionDto {

    @Schema(description = "Título de la sesión", example = "Keynote: El futuro de la Inteligencia Artificial", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "El título es obligatorio")
    @Size(min = 3, max = 200, message = "El título debe tener entre 3 y 200 caracteres")
    private String title;

    @Schema(description = "Descripción detallada de la sesión", example = "Charla de apertura sobre los últimos avances en LLMs")
    private String description;

    @Schema(description = "Fecha y hora de inicio de la sesión", example = "2026-08-15T09:00:00Z", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "La fecha/hora de inicio es obligatoria")
    private LocalDateTime startTime;

    @Schema(description = "Fecha y hora de finalización de la sesión", example = "2026-08-15T10:30:00Z", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "La fecha/hora de fin es obligatoria")
    private LocalDateTime endTime;

    @Schema(description = "Aula, sala o salón asignado", example = "Auditorio Principal (Bloque B)")
    private String room;

    @Schema(description = "ID del evento al que pertenece la sesión", example = "10", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "El evento asociado es obligatorio")
    private Long eventId;

    public CreateSessionDto() {
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

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }
}
