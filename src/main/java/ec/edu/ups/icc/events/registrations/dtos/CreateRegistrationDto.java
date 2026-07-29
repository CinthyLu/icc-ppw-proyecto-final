package ec.edu.ups.icc.events.registrations.dtos;

import jakarta.validation.constraints.NotNull;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "DTO para crear una inscripción a un evento")
public class CreateRegistrationDto {

    @Schema(description = "ID del evento al que se desea inscribir", example = "10", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "El id del evento es obligatorio")
    private Long eventId;

    public CreateRegistrationDto() {
    }

    public CreateRegistrationDto(Long eventId) {
        this.eventId = eventId;
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }
}
