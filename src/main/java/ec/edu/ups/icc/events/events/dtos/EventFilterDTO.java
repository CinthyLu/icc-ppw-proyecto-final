package ec.edu.ups.icc.events.events.dtos;

import ec.edu.ups.icc.events.events.entities.EventModality;

import java.time.LocalDateTime;

public record EventFilterDTO(
        String text,
        Long categoryId,
        EventModality modality,
        LocalDateTime startDate,
        LocalDateTime endDate
) {
}
