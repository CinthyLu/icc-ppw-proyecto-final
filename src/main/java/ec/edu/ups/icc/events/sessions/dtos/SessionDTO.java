package ec.edu.ups.icc.events.sessions.dtos;

import java.time.LocalDateTime;

public record SessionDTO(
        Long id,
        String title,
        String description,
        LocalDateTime startTime,
        LocalDateTime endTime,
        String room,
        Long eventId
) {
}
