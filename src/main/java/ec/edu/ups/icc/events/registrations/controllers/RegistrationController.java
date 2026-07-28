package ec.edu.ups.icc.events.registrations.controllers;

import ec.edu.ups.icc.events.registrations.dtos.RegistrationDTO;
import ec.edu.ups.icc.events.registrations.services.RegistrationService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/registrations")
public class RegistrationController {

    private final RegistrationService registrationService;

    public RegistrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @GetMapping
    public ResponseEntity<Page<RegistrationDTO>> getRegistrations(
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
    public ResponseEntity<RegistrationDTO> registerUserToEvent(@PathVariable Long eventId) {
        return ResponseEntity.ok(registrationService.registerUserToEvent(eventId));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<RegistrationDTO> cancelRegistration(@PathVariable Long id) {
        return ResponseEntity.ok(registrationService.cancelRegistration(id));
    }
}
