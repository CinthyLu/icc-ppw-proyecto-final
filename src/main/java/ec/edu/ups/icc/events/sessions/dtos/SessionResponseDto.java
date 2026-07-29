package ec.edu.ups.icc.events.sessions.dtos;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Respuesta detallada de una sesión o conferencia")
public class SessionResponseDto {

    @Schema(description = "ID único de la sesión", example = "100")
    private Long id;

    @Schema(description = "Título de la sesión", example = "Keynote: El futuro de la Inteligencia Artificial")
    private String title;

    @Schema(description = "Descripción detallada de la sesión", example = "Charla de apertura sobre los últimos avances en LLMs")
    private String description;

    @Schema(description = "Fecha y hora de inicio de la sesión", example = "2026-08-15T09:00:00Z")
    private LocalDateTime startTime;

    @Schema(description = "Fecha y hora de finalización de la sesión", example = "2026-08-15T10:30:00Z")
    private LocalDateTime endTime;

    @Schema(description = "Aula, sala o salón asignado", example = "Auditorio Principal (Bloque B)")
    private String room;

    @Schema(description = "ID del evento asociado", example = "10")
    private Long eventId;

    public SessionResponseDto() {
    }

    public SessionResponseDto(Long id, String title, String description, LocalDateTime startTime, LocalDateTime endTime, String room, Long eventId) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.startTime = startTime;
        this.endTime = endTime;
        this.room = room;
        this.eventId = eventId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
