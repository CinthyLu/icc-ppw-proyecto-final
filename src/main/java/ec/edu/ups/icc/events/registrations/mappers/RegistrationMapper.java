package ec.edu.ups.icc.events.registrations.mappers;

import ec.edu.ups.icc.events.registrations.dtos.RegistrationResponseDto;
import ec.edu.ups.icc.events.registrations.entities.RegistrationEntity;

public class RegistrationMapper {

    public static RegistrationResponseDto toResponse(RegistrationEntity entity) {
        if (entity == null) {
            return null;
        }
        RegistrationResponseDto dto = new RegistrationResponseDto();
        dto.setId(entity.getId());
        dto.setUserId(entity.getUser() != null ? entity.getUser().getId() : null);
        dto.setUserEmail(entity.getUser() != null ? entity.getUser().getEmail() : null);
        dto.setEventId(entity.getEvent() != null ? entity.getEvent().getId() : null);
        dto.setEventTitle(entity.getEvent() != null ? entity.getEvent().getTitle() : null);
        dto.setRegistrationDate(entity.getRegistrationDate());
        dto.setStatus(entity.getStatus());
        return dto;
    }
}
