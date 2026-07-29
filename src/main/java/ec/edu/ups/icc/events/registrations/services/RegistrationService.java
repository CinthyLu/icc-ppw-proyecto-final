package ec.edu.ups.icc.events.registrations.services;

import ec.edu.ups.icc.events.registrations.dtos.RegistrationResponseDto;
import org.springframework.data.domain.Page;

public interface RegistrationService {
    RegistrationResponseDto registerUserToEvent(Long eventId);
    RegistrationResponseDto cancelRegistration(Long id);
    Page<RegistrationResponseDto> getRegistrations(int page, int size, Long eventId, String status, String sortBy, String sortDir);
}
