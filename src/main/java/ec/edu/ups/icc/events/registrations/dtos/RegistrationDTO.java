package ec.edu.ups.icc.events.registrations.dtos;

import ec.edu.ups.icc.events.registrations.entities.RegistrationStatus;

import java.time.LocalDateTime;

public record RegistrationDTO(
        Long id,
        Long userId,
        String userEmail,
        Long eventId,
        String eventTitle,
        LocalDateTime registrationDate,
        RegistrationStatus status
) {
}

