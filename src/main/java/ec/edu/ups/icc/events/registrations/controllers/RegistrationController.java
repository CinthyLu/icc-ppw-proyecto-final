package ec.edu.ups.icc.events.registrations.controllers;

import ec.edu.ups.icc.events.registrations.dtos.RegistrationResponseDto;
import ec.edu.ups.icc.events.registrations.services.RegistrationService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/registrations")
@Tag(name = "Inscripciones", description = "Módulo para la inscripción de participantes en eventos y control de cupos")
public class RegistrationController {

    private final RegistrationService registrationService;

    public RegistrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Obtener todas las inscripciones paginadas", description = "Permite a los administradores listar y filtrar todas las inscripciones del sistema. (Solo administradores).", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Page<RegistrationResponseDto>> getRegistrations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long eventId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false, defaultValue = "registrationDate") String sortBy,
            @RequestParam(required = false, defaultValue = "desc") String sortDir
    ) {
        return ResponseEntity.ok(registrationService.getRegistrations(page, size, eventId, status, sortBy, sortDir));
    }

    @PostMapping("/events/{eventId}")
    @PreAuthorize("hasRole('PARTICIPANT')")
    @Operation(summary = "Inscribirse a un evento", description = "Inscribe al participante autenticado en el evento indicado. (Solo participantes).", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<RegistrationResponseDto> registerUserToEvent(@PathVariable Long eventId) {
        return ResponseEntity.ok(registrationService.registerUserToEvent(eventId));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'PARTICIPANT')")
    @Operation(summary = "Cancelar una inscripción", description = "Cancela la inscripción de un usuario a un evento liberando el cupo respectivo. (Solo el participante dueño o administradores).", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<RegistrationResponseDto> cancelRegistration(@PathVariable Long id) {
        return ResponseEntity.ok(registrationService.cancelRegistration(id));
    }
}
