package ec.edu.ups.icc.events.sessions.services;

import ec.edu.ups.icc.events.sessions.dtos.CreateSessionDto;
import ec.edu.ups.icc.events.sessions.dtos.SessionResponseDto;

import java.util.List;

public interface SessionService {
    List<SessionResponseDto> getSessionsByEventId(Long eventId);
    SessionResponseDto getSessionById(Long id);
    SessionResponseDto createSession(Long eventId, CreateSessionDto dto);
    SessionResponseDto updateSession(Long id, CreateSessionDto dto);
    void deleteSession(Long id);
}
