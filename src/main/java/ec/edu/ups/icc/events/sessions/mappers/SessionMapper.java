package ec.edu.ups.icc.events.sessions.mappers;

import ec.edu.ups.icc.events.sessions.dtos.CreateSessionDto;
import ec.edu.ups.icc.events.sessions.dtos.SessionResponseDto;
import ec.edu.ups.icc.events.sessions.entities.SessionEntity;

public class SessionMapper {

    public static SessionEntity toEntity(CreateSessionDto dto) {
        if (dto == null) {
            return null;
        }
        SessionEntity entity = new SessionEntity();
        entity.setTitle(dto.getTitle());
        entity.setDescription(dto.getDescription());
        entity.setStartTime(dto.getStartTime());
        entity.setEndTime(dto.getEndTime());
        entity.setRoom(dto.getRoom());
        return entity;
    }

    public static void updateEntity(CreateSessionDto dto, SessionEntity entity) {
        if (dto == null || entity == null) {
            return;
        }
        entity.setTitle(dto.getTitle());
        entity.setDescription(dto.getDescription());
        entity.setStartTime(dto.getStartTime());
        entity.setEndTime(dto.getEndTime());
        entity.setRoom(dto.getRoom());
    }

    public static SessionResponseDto toResponse(SessionEntity entity) {
        if (entity == null) {
            return null;
        }
        SessionResponseDto dto = new SessionResponseDto();
        dto.setId(entity.getId());
        dto.setTitle(entity.getTitle());
        dto.setDescription(entity.getDescription());
        dto.setStartTime(entity.getStartTime());
        dto.setEndTime(entity.getEndTime());
        dto.setRoom(entity.getRoom());
        dto.setEventId(entity.getEvent() != null ? entity.getEvent().getId() : null);
        return dto;
    }
}
