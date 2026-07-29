package ec.edu.ups.icc.events.registrations.dtos;

import jakarta.validation.constraints.NotNull;

public class CreateRegistrationDto {

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
