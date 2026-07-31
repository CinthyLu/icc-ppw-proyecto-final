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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Tag(name = "Sesiones", description = "Módulo de gestión de sesiones y conferencias por cada evento")
public class SessionController {

    private final SessionService sessionService;

    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @GetMapping("/events/{eventId}/sessions")
    @Operation(summary = "Obtener sesiones por Evento ID", description = "Obtiene la lista de todas las sesiones asociadas a un evento en particular (público).")
    public ResponseEntity<List<SessionResponseDto>> getSessionsByEventId(@PathVariable Long eventId) {
        return ResponseEntity.ok(sessionService.getSessionsByEventId(eventId));
    }

    @GetMapping("/sessions/{id}")
    @Operation(summary = "Obtener sesión por ID", description = "Obtiene los detalles específicos de una sesión por su ID (público).")
    public ResponseEntity<SessionResponseDto> getSessionById(@PathVariable Long id) {
        return ResponseEntity.ok(sessionService.getSessionById(id));
    }

    @PostMapping("/events/{eventId}/sessions")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    @Operation(summary = "Crear sesión en evento", description = "Agrega una nueva sesión o conferencia dentro de un evento existente. (Solo administradores y organizadores autorizados).", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<SessionResponseDto> createSession(
            @PathVariable Long eventId,
            @Valid @RequestBody CreateSessionDto dto) {
        SessionResponseDto created = sessionService.createSession(eventId, dto);
        return ResponseEntity.created(URI.create("/api/sessions/" + created.getId())).body(created);
    }

    @PutMapping("/sessions/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    @Operation(summary = "Actualizar sesión por ID", description = "Actualiza los detalles (horario, salón, título) de una sesión. (Solo administradores y organizadores autorizados).", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<SessionResponseDto> updateSession(
            @PathVariable Long id,
            @Valid @RequestBody CreateSessionDto dto) {
        return ResponseEntity.ok(sessionService.updateSession(id, dto));
    }

    @DeleteMapping("/sessions/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    @Operation(summary = "Eliminar sesión por ID", description = "Elimina físicamente una sesión del evento. (Solo administradores y organizadores autorizados).", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Void> deleteSession(@PathVariable Long id) {
        sessionService.deleteSession(id);
        return ResponseEntity.noContent().build();
    }
}
