package ec.edu.ups.icc.events.events.controllers;

import ec.edu.ups.icc.events.events.dtos.EventDTO;
import ec.edu.ups.icc.events.events.dtos.EventFilterDTO;
import ec.edu.ups.icc.events.events.services.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/events")
@Tag(name = "Eventos", description = "Gestión de eventos académicos")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @Operation(summary = "Buscar eventos", description = "Retorna eventos con paginación, filtros, búsqueda y ordenamiento.", responses = {
            @ApiResponse(responseCode = "200", description = "Listado de eventos", content = @Content(mediaType = "application/json", schema = @Schema(implementation = EventDTO.class), examples = @ExampleObject(value = "{\"content\":[{\"id\":1,\"title\":\"Charla\",\"status\":\"PUBLISHED\"}],\"totalElements\":1}")))
    })
    @GetMapping
    public ResponseEntity<Page<EventDTO>> searchEvents(
            @Parameter(description = "Texto libre para buscar por título, descripción o ubicación") @RequestParam(required = false) String text,
            @Parameter(description = "Filtrar por categoría") @RequestParam(required = false) Long categoryId,
            @Parameter(description = "Filtrar por modalidad") @RequestParam(required = false) String modality,
            @Parameter(description = "Fecha mínima de inicio") @RequestParam(required = false) String startDate,
            @Parameter(description = "Fecha máxima de fin") @RequestParam(required = false) String endDate,
            @Parameter(description = "Número de página") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Campo por el que ordenar") @RequestParam(required = false, defaultValue = "startDate") String sortBy,
            @Parameter(description = "Dirección del orden: asc o desc") @RequestParam(required = false, defaultValue = "asc") String sortDir
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

    @Operation(summary = "Obtener evento por id", description = "Retorna un evento por su identificador.", responses = {
            @ApiResponse(responseCode = "200", description = "Evento encontrado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = EventDTO.class))),
            @ApiResponse(responseCode = "404", description = "No existe el evento")
    })
    @GetMapping("/{id}")
    public ResponseEntity<EventDTO> getEventById(@Parameter(description = "Identificador del evento") @PathVariable Long id) {
        return ResponseEntity.ok(eventService.getEventById(id));
    }

    @Operation(summary = "Crear evento", description = "Crea un evento nuevo. Requiere rol ADMIN u ORGANIZER.", security = @SecurityRequirement(name = "bearerAuth"), responses = {
            @ApiResponse(responseCode = "201", description = "Evento creado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = EventDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos")
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public ResponseEntity<EventDTO> createEvent(@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Datos del evento a crear", required = true) @RequestBody EventDTO dto) {
        EventDTO created = eventService.createEvent(dto);
        return ResponseEntity.created(URI.create("/api/events/" + created.id())).body(created);
    }

    @Operation(summary = "Actualizar evento", description = "Actualiza un evento existente. Requiere rol ADMIN u ORGANIZER.", security = @SecurityRequirement(name = "bearerAuth"), responses = {
            @ApiResponse(responseCode = "200", description = "Evento actualizado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = EventDTO.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos"),
            @ApiResponse(responseCode = "404", description = "No existe el evento")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public ResponseEntity<EventDTO> updateEvent(@Parameter(description = "Identificador del evento") @PathVariable Long id, @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Datos actualizados del evento", required = true) @RequestBody EventDTO dto) {
        return ResponseEntity.ok(eventService.updateEvent(id, dto));
    }

    @Operation(summary = "Eliminar evento", description = "Marca el evento como cancelado sin borrarlo físicamente. Requiere rol ADMIN u ORGANIZER.", security = @SecurityRequirement(name = "bearerAuth"), responses = {
            @ApiResponse(responseCode = "204", description = "Evento eliminado lógicamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos"),
            @ApiResponse(responseCode = "404", description = "No existe el evento")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public ResponseEntity<Void> deleteEvent(@Parameter(description = "Identificador del evento") @PathVariable Long id) {
        eventService.deleteEvent(id);
        return ResponseEntity.noContent().build();
    }
}
