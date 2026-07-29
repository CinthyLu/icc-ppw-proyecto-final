package ec.edu.ups.icc.events.events.controllers;

import ec.edu.ups.icc.events.events.dtos.CreateEventDto;
import ec.edu.ups.icc.events.events.dtos.EventFilterDTO;
import ec.edu.ups.icc.events.events.dtos.EventResponseDto;
import ec.edu.ups.icc.events.events.dtos.UpdateEventDto;
import ec.edu.ups.icc.events.events.services.EventService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/events")
@Tag(name = "Eventos", description = "Módulo de gestión de eventos académicos, conferencias y congresos")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping
    @Operation(summary = "Buscar y filtrar eventos paginados", description = "Obtiene una lista paginada de eventos aplicando filtros dinámicos como texto, categoría, modalidad y fechas (público).")
    public ResponseEntity<Page<EventResponseDto>> searchEvents(
            @RequestParam(required = false) String text,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String modality,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false, defaultValue = "startDate") String sortBy,
            @RequestParam(required = false, defaultValue = "asc") String sortDir
    ) {
        EventFilterDTO filter = new EventFilterDTO(
                text,
                categoryId,
                modality != null ? ec.edu.ups.icc.events.events.entities.EventModality.valueOf(modality.toUpperCase()) : null,
                startDate != null ? java.time.LocalDateTime.parse(startDate) : null,
                endDate != null ? java.time.LocalDateTime.parse(endDate) : null
        );
        return ResponseEntity.ok(eventService.searchEvents(filter, page, size, sortBy, sortDir));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener evento por ID", description = "Obtiene los detalles detallados de un evento académico por su ID (público).")
    public ResponseEntity<EventResponseDto> getEventById(@PathVariable Long id) {
        return ResponseEntity.ok(eventService.getEventById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    @Operation(summary = "Crear nuevo evento", description = "Crea un nuevo evento académico. (Solo administradores y organizadores autorizados).", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<EventResponseDto> createEvent(@Valid @RequestBody CreateEventDto dto) {
        EventResponseDto created = eventService.createEvent(dto);
        return ResponseEntity.created(URI.create("/api/events/" + created.getId())).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    @Operation(summary = "Actualizar evento por ID", description = "Actualiza los campos de un evento. El organizador creador o un administrador son los únicos autorizados. (Solo administradores y organizadores).", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<EventResponseDto> updateEvent(
            @PathVariable Long id,
            @Valid @RequestBody UpdateEventDto dto
    ) {
        return ResponseEntity.ok(eventService.updateEvent(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    @Operation(summary = "Eliminar evento por ID", description = "Elimina de forma lógica (soft-delete) un evento del sistema. (Solo administradores y organizadores autorizados).", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id) {
        eventService.deleteEvent(id);
        return ResponseEntity.noContent().build();
    }
}
