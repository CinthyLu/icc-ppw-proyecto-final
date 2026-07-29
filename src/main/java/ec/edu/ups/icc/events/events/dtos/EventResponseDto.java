package ec.edu.ups.icc.events.events.dtos;

import ec.edu.ups.icc.events.events.entities.EventModality;
import ec.edu.ups.icc.events.events.entities.EventStatus;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Respuesta detallada de un evento académico")
public class EventResponseDto {

    @Schema(description = "ID único del evento", example = "10")
    private Long id;

    @Schema(description = "Título del evento", example = "Congreso de Ciencias de la Computación 2026")
    private String title;

    @Schema(description = "Descripción detallada del evento", example = "Un espacio de divulgación y conferencias sobre avances tecnológicos.")
    private String description;

    @Schema(description = "Modalidad del evento (PRESENTIAL, VIRTUAL, HYBRID)", example = "PRESENTIAL")
    private EventModality modality;

    @Schema(description = "Lugar o dirección física", example = "Auditorio Generalups, Campus Girón")
    private String location;

    @Schema(description = "Capacidad máxima de asistentes", example = "150")
    private Integer capacity;

    @Schema(description = "Asientos o cupos disponibles actualmente", example = "142")
    private Integer availableSeats;

    @Schema(description = "Fecha y hora de inicio del evento", example = "2026-09-20T08:00:00Z")
    private LocalDateTime startDate;

    @Schema(description = "Fecha y hora de finalización del evento", example = "2026-09-22T17:00:00Z")
    private LocalDateTime endDate;

    @Schema(description = "Estado actual del evento (DRAFT, PUBLISHED, CANCELLED)", example = "PUBLISHED")
    private EventStatus status;

    @Schema(description = "ID del usuario organizador del evento", example = "3")
    private Long organizerId;

    @Schema(description = "ID de la categoría del evento", example = "2")
    private Long categoryId;

    public EventResponseDto() {
    }

    public EventResponseDto(Long id, String title, String description, EventModality modality, String location, Integer capacity, Integer availableSeats, LocalDateTime startDate, LocalDateTime endDate, EventStatus status, Long organizerId, Long categoryId) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.modality = modality;
        this.location = location;
        this.capacity = capacity;
        this.availableSeats = availableSeats;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.organizerId = organizerId;
        this.categoryId = categoryId;
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

    public Integer getAvailableSeats() {
        return availableSeats;
    }

    public void setAvailableSeats(Integer availableSeats) {
        this.availableSeats = availableSeats;
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

    public EventStatus getStatus() {
        return status;
    }

    public void setStatus(EventStatus status) {
        this.status = status;
    }

    public Long getOrganizerId() {
        return organizerId;
    }

    public void setOrganizerId(Long organizerId) {
        this.organizerId = organizerId;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }
}
