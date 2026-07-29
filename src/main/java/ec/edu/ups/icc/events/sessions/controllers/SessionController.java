package ec.edu.ups.icc.events.sessions.controllers;

import ec.edu.ups.icc.events.sessions.dtos.SessionDTO;
import ec.edu.ups.icc.events.sessions.services.SessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api")
@Tag(name = "Sesiones", description = "Gestión de sesiones por evento")
public class SessionController {

    private final SessionService sessionService;

    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @Operation(summary = "Listar sesiones por evento", description = "Retorna las sesiones asociadas a un evento.", responses = {
            @ApiResponse(responseCode = "200", description = "Listado de sesiones", content = @Content(mediaType = "application/json", schema = @Schema(implementation = SessionDTO.class)))
    })
    @GetMapping("/events/{eventId}/sessions")
    public ResponseEntity<List<SessionDTO>> getSessionsByEventId(@Parameter(description = "Identificador del evento") @PathVariable Long eventId) {
        return ResponseEntity.ok(sessionService.getSessionsByEventId(eventId));
    }

    @Operation(summary = "Obtener sesión por id", description = "Retorna una sesión específica.", responses = {
            @ApiResponse(responseCode = "200", description = "Sesión encontrada", content = @Content(mediaType = "application/json", schema = @Schema(implementation = SessionDTO.class))),
            @ApiResponse(responseCode = "404", description = "No existe la sesión")
    })
    @GetMapping("/sessions/{id}")
    public ResponseEntity<SessionDTO> getSessionById(@Parameter(description = "Identificador de la sesión") @PathVariable Long id) {
        return ResponseEntity.ok(sessionService.getSessionById(id));
    }

    @Operation(summary = "Crear sesión", description = "Crea una sesión para un evento. Requiere rol ADMIN u ORGANIZER.", security = @SecurityRequirement(name = "bearerAuth"), responses = {
            @ApiResponse(responseCode = "201", description = "Sesión creada", content = @Content(mediaType = "application/json", schema = @Schema(implementation = SessionDTO.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos")
    })
    @PostMapping("/events/{eventId}/sessions")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public ResponseEntity<SessionDTO> createSession(@Parameter(description = "Identificador del evento") @PathVariable Long eventId, @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Datos de la sesión", required = true) @RequestBody SessionDTO dto) {
        SessionDTO created = sessionService.createSession(eventId, dto);
        return ResponseEntity.created(URI.create("/api/sessions/" + created.id())).body(created);
    }

    @Operation(summary = "Actualizar sesión", description = "Actualiza una sesión existente. Requiere rol ADMIN u ORGANIZER.", security = @SecurityRequirement(name = "bearerAuth"), responses = {
            @ApiResponse(responseCode = "200", description = "Sesión actualizada", content = @Content(mediaType = "application/json", schema = @Schema(implementation = SessionDTO.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos")
    })
    @PutMapping("/sessions/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public ResponseEntity<SessionDTO> updateSession(@Parameter(description = "Identificador de la sesión") @PathVariable Long id, @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Datos actualizados de la sesión", required = true) @RequestBody SessionDTO dto) {
        return ResponseEntity.ok(sessionService.updateSession(id, dto));
    }

    @Operation(summary = "Eliminar sesión", description = "Elimina una sesión. Requiere rol ADMIN u ORGANIZER.", security = @SecurityRequirement(name = "bearerAuth"), responses = {
            @ApiResponse(responseCode = "204", description = "Sesión eliminada"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos")
    })
    @DeleteMapping("/sessions/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public ResponseEntity<Void> deleteSession(@Parameter(description = "Identificador de la sesión") @PathVariable Long id) {
        sessionService.deleteSession(id);
        return ResponseEntity.noContent().build();
    }
}
