package ec.edu.ups.icc.events.events.services;

import ec.edu.ups.icc.events.events.dtos.CreateEventDto;

import ec.edu.ups.icc.events.events.dtos.EventFilterDTO;
import ec.edu.ups.icc.events.events.dtos.EventResponseDto;
import ec.edu.ups.icc.events.events.dtos.UpdateEventDto;

import org.springframework.data.domain.Page;


public interface EventService {
    Page<EventResponseDto> searchEvents(EventFilterDTO filter, int page, int size, String sortBy, String sortDir);
    EventResponseDto getEventById(Long id);
    EventResponseDto createEvent(CreateEventDto dto);
    EventResponseDto updateEvent(Long id, UpdateEventDto dto);
    void deleteEvent(Long id);
}
