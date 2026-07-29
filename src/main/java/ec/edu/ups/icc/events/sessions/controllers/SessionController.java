package ec.edu.ups.icc.events.sessions.controllers;

import ec.edu.ups.icc.events.sessions.dtos.CreateSessionDto;
import ec.edu.ups.icc.events.sessions.dtos.SessionResponseDto;
import ec.edu.ups.icc.events.sessions.services.SessionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api")
public class SessionController {

    private final SessionService sessionService;

    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @GetMapping("/events/{eventId}/sessions")
    public ResponseEntity<List<SessionResponseDto>> getSessionsByEventId(@PathVariable Long eventId) {
        return ResponseEntity.ok(sessionService.getSessionsByEventId(eventId));
    }

    @GetMapping("/sessions/{id}")
    public ResponseEntity<SessionResponseDto> getSessionById(@PathVariable Long id) {
        return ResponseEntity.ok(sessionService.getSessionById(id));
    }

    @PostMapping("/events/{eventId}/sessions")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public ResponseEntity<SessionResponseDto> createSession(
            @PathVariable Long eventId,
            @Valid @RequestBody CreateSessionDto dto) {
        SessionResponseDto created = sessionService.createSession(eventId, dto);
        return ResponseEntity.created(URI.create("/api/sessions/" + created.getId())).body(created);
    }

    @PutMapping("/sessions/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public ResponseEntity<SessionResponseDto> updateSession(
            @PathVariable Long id,
            @Valid @RequestBody CreateSessionDto dto) {
        return ResponseEntity.ok(sessionService.updateSession(id, dto));
    }

    @DeleteMapping("/sessions/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public ResponseEntity<Void> deleteSession(@PathVariable Long id) {
        sessionService.deleteSession(id);
        return ResponseEntity.noContent().build();
    }
}
