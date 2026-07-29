package ec.edu.ups.icc.events.events.mappers;

import ec.edu.ups.icc.events.events.dtos.CreateEventDto;
import ec.edu.ups.icc.events.events.dtos.EventResponseDto;
import ec.edu.ups.icc.events.events.dtos.UpdateEventDto;
import ec.edu.ups.icc.events.events.entities.EventEntity;

public class EventMapper {

    public static EventEntity toEntity(CreateEventDto dto) {
        if (dto == null) {
            return null;
        }
        EventEntity entity = new EventEntity();
        entity.setTitle(dto.getTitle());
        entity.setDescription(dto.getDescription());
        entity.setModality(dto.getModality());
        entity.setLocation(dto.getLocation());
        entity.setCapacity(dto.getCapacity());
        entity.setAvailableSeats(dto.getCapacity());
        entity.setStartDate(dto.getStartDate());
        entity.setEndDate(dto.getEndDate());
        return entity;
    }

    public static void updateEntity(UpdateEventDto dto, EventEntity entity) {
        if (dto == null || entity == null) {
            return;
        }
        int diff = dto.getCapacity() - entity.getCapacity();
        entity.setTitle(dto.getTitle());
        entity.setDescription(dto.getDescription());
        entity.setModality(dto.getModality());
        entity.setLocation(dto.getLocation());
        entity.setCapacity(dto.getCapacity());
        entity.setAvailableSeats(entity.getAvailableSeats() + diff);
        entity.setStartDate(dto.getStartDate());
        entity.setEndDate(dto.getEndDate());
        entity.setStatus(dto.getStatus());
    }

    public static EventResponseDto toResponse(EventEntity entity) {
        if (entity == null) {
            return null;
        }
        EventResponseDto dto = new EventResponseDto();
        dto.setId(entity.getId());
        dto.setTitle(entity.getTitle());
        dto.setDescription(entity.getDescription());
        dto.setModality(entity.getModality());
        dto.setLocation(entity.getLocation());
        dto.setCapacity(entity.getCapacity());
        dto.setAvailableSeats(entity.getAvailableSeats());
        dto.setStartDate(entity.getStartDate());
        dto.setEndDate(entity.getEndDate());
        dto.setStatus(entity.getStatus());
        dto.setOrganizerId(entity.getOrganizer() != null ? entity.getOrganizer().getId() : null);
        dto.setCategoryId(entity.getCategory() != null ? entity.getCategory().getId() : null);
        return dto;
    }
}
