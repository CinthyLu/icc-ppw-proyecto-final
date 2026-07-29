package ec.edu.ups.icc.events.registrations.dtos;

import ec.edu.ups.icc.events.registrations.entities.RegistrationStatus;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Respuesta detallada de una inscripción")
public class RegistrationResponseDto {

    @Schema(description = "ID único de la inscripción", example = "50")
    private Long id;

    @Schema(description = "ID del usuario inscrito", example = "1")
    private Long userId;

    @Schema(description = "Correo del usuario inscrito", example = "student@ups.edu.ec")
    private String userEmail;

    @Schema(description = "ID del evento", example = "10")
    private Long eventId;

    @Schema(description = "Título del evento", example = "Congreso de Ingeniería de Software 2026")
    private String eventTitle;

    @Schema(description = "Fecha y hora en la que se realizó la inscripción", example = "2026-08-01T14:32:00Z")
    private LocalDateTime registrationDate;

    @Schema(description = "Estado de la inscripción (CONFIRMED, CANCELLED)", example = "CONFIRMED")
    private RegistrationStatus status;

    public RegistrationResponseDto() {
    }

    public RegistrationResponseDto(Long id, Long userId, String userEmail, Long eventId, String eventTitle, LocalDateTime registrationDate, RegistrationStatus status) {
        this.id = id;
        this.userId = userId;
        this.userEmail = userEmail;
        this.eventId = eventId;
        this.eventTitle = eventTitle;
        this.registrationDate = registrationDate;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public String getEventTitle() {
        return eventTitle;
    }

    public void setEventTitle(String eventTitle) {
        this.eventTitle = eventTitle;
    }

    public LocalDateTime getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(LocalDateTime registrationDate) {
        this.registrationDate = registrationDate;
    }

    public RegistrationStatus getStatus() {
        return status;
    }

    public void setStatus(RegistrationStatus status) {
        this.status = status;
    }
}
