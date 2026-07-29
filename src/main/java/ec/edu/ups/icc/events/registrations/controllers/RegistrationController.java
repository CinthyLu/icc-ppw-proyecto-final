package ec.edu.ups.icc.events.registrations.controllers;

import ec.edu.ups.icc.events.registrations.dtos.RegistrationDTO;
import ec.edu.ups.icc.events.registrations.services.RegistrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/registrations")
@Tag(name = "Inscripciones", description = "Gestión de inscripciones a eventos")
public class RegistrationController {

    private final RegistrationService registrationService;

    public RegistrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @Operation(summary = "Listar inscripciones", description = "Devuelve las inscripciones según el usuario autenticado y los filtros proporcionados.", security = @SecurityRequirement(name = "bearerAuth"), responses = {
            @ApiResponse(responseCode = "200", description = "Listado de inscripciones", content = @Content(mediaType = "application/json", schema = @Schema(implementation = RegistrationDTO.class)))
    })
    @GetMapping
    public ResponseEntity<Page<RegistrationDTO>> getRegistrations(
            @Parameter(description = "Número de página") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Filtrar por evento") @RequestParam(required = false) Long eventId,
            @Parameter(description = "Filtrar por estado") @RequestParam(required = false) String status,
            @Parameter(description = "Campo por el que ordenar") @RequestParam(required = false, defaultValue = "registrationDate") String sortBy,
            @Parameter(description = "Dirección del orden") @RequestParam(required = false, defaultValue = "desc") String sortDir
    ) {
        return ResponseEntity.ok(registrationService.getRegistrations(page, size, eventId, status, sortBy, sortDir));
    }

    @Operation(summary = "Inscribirse a un evento", description = "Crea una inscripción para el usuario autenticado en un evento publicado y con cupos disponibles.", security = @SecurityRequirement(name = "bearerAuth"), responses = {
            @ApiResponse(responseCode = "200", description = "Inscripción creada", content = @Content(mediaType = "application/json", schema = @Schema(implementation = RegistrationDTO.class))),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "409", description = "Regla de negocio violada")
    })
    @PostMapping("/events/{eventId}")
    public ResponseEntity<RegistrationDTO> registerUserToEvent(@Parameter(description = "Identificador del evento") @PathVariable Long eventId) {
        return ResponseEntity.ok(registrationService.registerUserToEvent(eventId));
    }

    @Operation(summary = "Cancelar inscripción", description = "Cancela una inscripción existente del usuario autenticado.", security = @SecurityRequirement(name = "bearerAuth"), responses = {
            @ApiResponse(responseCode = "200", description = "Inscripción cancelada", content = @Content(mediaType = "application/json", schema = @Schema(implementation = RegistrationDTO.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos"),
            @ApiResponse(responseCode = "404", description = "Inscripción no encontrada")
    })
    @PostMapping("/{id}/cancel")
    public ResponseEntity<RegistrationDTO> cancelRegistration(@Parameter(description = "Identificador de la inscripción") @PathVariable Long id) {
        return ResponseEntity.ok(registrationService.cancelRegistration(id));
    }
}
