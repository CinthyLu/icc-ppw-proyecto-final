package ec.edu.ups.icc.events.sessions.services;

import ec.edu.ups.icc.events.core.exceptions.ForbiddenException;
import ec.edu.ups.icc.events.core.exceptions.ResourceNotFoundException;
import ec.edu.ups.icc.events.core.exceptions.UnauthorizedException;
import ec.edu.ups.icc.events.events.entities.EventEntity;
import ec.edu.ups.icc.events.events.repositories.EventRepository;
import ec.edu.ups.icc.events.sessions.dtos.SessionDTO;
import ec.edu.ups.icc.events.sessions.entities.SessionEntity;
import ec.edu.ups.icc.events.sessions.repositories.SessionRepository;
import ec.edu.ups.icc.events.users.repositories.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class SessionService {

    private final SessionRepository sessionRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    public SessionService(SessionRepository sessionRepository,
                          EventRepository eventRepository,
                          UserRepository userRepository) {
        this.sessionRepository = sessionRepository;
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
    }

    public List<SessionDTO> getSessionsByEventId(Long eventId) {
        if (!eventRepository.existsById(eventId)) {
            throw new ResourceNotFoundException("Evento no encontrado con id: " + eventId);
        }
        return sessionRepository.findByEventId(eventId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public SessionDTO getSessionById(Long id) {
        return sessionRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Sesión no encontrada con id: " + id));
    }

    public SessionDTO createSession(Long eventId, SessionDTO dto) {
        EventEntity event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Evento no encontrado con id: " + eventId));
        
        verifyEventOrganizer(event);

        if (dto.title() == null || dto.title().isBlank()) {
            throw new IllegalArgumentException("El título de la sesión es obligatorio");
        }
        if (dto.startTime() == null || dto.endTime() == null) {
            throw new IllegalArgumentException("La fecha de inicio y fin son obligatorias");
        }
        if (dto.endTime().isBefore(dto.startTime())) {
            throw new IllegalArgumentException("La fecha de fin debe ser posterior a la de inicio");
        }

        SessionEntity session = new SessionEntity();
        session.setTitle(dto.title());
        session.setDescription(dto.description());
        session.setStartTime(dto.startTime());
        session.setEndTime(dto.endTime());
        session.setRoom(dto.room());
        session.setEvent(event);

        return toDto(sessionRepository.save(session));
    }

    public SessionDTO updateSession(Long id, SessionDTO dto) {
        SessionEntity session = sessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sesión no encontrada con id: " + id));
        
        verifyEventOrganizer(session.getEvent());

        if (dto.title() != null && !dto.title().isBlank()) {
            session.setTitle(dto.title());
        }
        session.setDescription(dto.description());
        if (dto.startTime() != null) {
            session.setStartTime(dto.startTime());
        }
        if (dto.endTime() != null) {
            session.setEndTime(dto.endTime());
        }
        if (session.getEndTime().isBefore(session.getStartTime())) {
            throw new IllegalArgumentException("La fecha de fin debe ser posterior a la de inicio");
        }
        session.setRoom(dto.room());

        return toDto(sessionRepository.save(session));
    }

    public void deleteSession(Long id) {
        SessionEntity session = sessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sesión no encontrada con id: " + id));
        
        verifyEventOrganizer(session.getEvent());
        
        sessionRepository.delete(session);
    }

    private void verifyEventOrganizer(EventEntity event) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String email;
        if (principal instanceof org.springframework.security.core.userdetails.User userDetails) {
            email = userDetails.getUsername();
        } else {
            throw new UnauthorizedException("Usuario no autenticado");
        }

        boolean isAdmin = SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin && !event.getOrganizer().getEmail().equals(email)) {
            throw new ForbiddenException("No tienes permisos de organizador para gestionar sesiones en este evento");
        }
    }

    private SessionDTO toDto(SessionEntity session) {
        return new SessionDTO(
                session.getId(),
                session.getTitle(),
                session.getDescription(),
                session.getStartTime(),
                session.getEndTime(),
                session.getRoom(),
                session.getEvent().getId()
        );
    }
}
