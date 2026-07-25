package ec.edu.ups.icc.events.events.dtos;

import ec.edu.ups.icc.events.events.entities.EventModality;
import ec.edu.ups.icc.events.events.entities.EventStatus;

import java.time.LocalDateTime;

public record EventDTO(
        Long id,
        String title,
        String description,
        EventModality modality,
        String location,
        Integer capacity,
        Integer availableSeats,
        LocalDateTime startDate,
        LocalDateTime endDate,
        EventStatus status,
        Long organizerId,
        Long categoryId
) {
}
