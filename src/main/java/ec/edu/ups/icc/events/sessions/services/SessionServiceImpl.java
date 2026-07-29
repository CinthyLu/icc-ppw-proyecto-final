package ec.edu.ups.icc.events.sessions.services;

import ec.edu.ups.icc.events.core.exceptions.BadRequestException;
import ec.edu.ups.icc.events.core.exceptions.ForbiddenException;
import ec.edu.ups.icc.events.core.exceptions.ResourceNotFoundException;
import ec.edu.ups.icc.events.core.exceptions.UnauthorizedException;
import ec.edu.ups.icc.events.events.entities.EventEntity;
import ec.edu.ups.icc.events.events.repositories.EventRepository;
import ec.edu.ups.icc.events.sessions.dtos.CreateSessionDto;
import ec.edu.ups.icc.events.sessions.dtos.SessionResponseDto;
import ec.edu.ups.icc.events.sessions.entities.SessionEntity;
import ec.edu.ups.icc.events.sessions.mappers.SessionMapper;
import ec.edu.ups.icc.events.sessions.repositories.SessionRepository;
import ec.edu.ups.icc.events.sessions.services.SessionService;
import ec.edu.ups.icc.events.users.repositories.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class SessionServiceImpl implements SessionService {

    private final SessionRepository sessionRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    public SessionServiceImpl(
            SessionRepository sessionRepository,
            EventRepository eventRepository,
            UserRepository userRepository
    ) {
        this.sessionRepository = sessionRepository;
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
    }

    @Override
    public List<SessionResponseDto> getSessionsByEventId(Long eventId) {
        if (!eventRepository.existsById(eventId)) {
            throw new ResourceNotFoundException("Evento no encontrado con id: " + eventId);
        }
        return sessionRepository.findByEventId(eventId).stream()
                .map(SessionMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public SessionResponseDto getSessionById(Long id) {
        return sessionRepository.findById(id)
                .map(SessionMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Sesión no encontrada con id: " + id));
    }

    @Override
    public SessionResponseDto createSession(Long eventId, CreateSessionDto dto) {
        EventEntity event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Evento no encontrado con id: " + eventId));

        verifyEventOrganizer(event);

        if (dto.getStartTime() == null || dto.getEndTime() == null) {
            throw new BadRequestException("La fecha de inicio y fin son obligatorias");
        }
        if (dto.getEndTime().isBefore(dto.getStartTime())) {
            throw new BadRequestException("La fecha de fin debe ser posterior a la de inicio");
        }

        SessionEntity session = SessionMapper.toEntity(dto);
        session.setEvent(event);

        return SessionMapper.toResponse(sessionRepository.save(session));
    }

    @Override
    public SessionResponseDto updateSession(Long id, CreateSessionDto dto) {
        SessionEntity session = sessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sesión no encontrada con id: " + id));

        verifyEventOrganizer(session.getEvent());

        SessionMapper.updateEntity(dto, session);
        if (session.getEndTime().isBefore(session.getStartTime())) {
            throw new BadRequestException("La fecha de fin debe ser posterior a la de inicio");
        }

        return SessionMapper.toResponse(sessionRepository.save(session));
    }

    @Override
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
}
