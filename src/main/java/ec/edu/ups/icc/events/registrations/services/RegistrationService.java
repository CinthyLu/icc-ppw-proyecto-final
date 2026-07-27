package ec.edu.ups.icc.events.registrations.services;

import ec.edu.ups.icc.events.core.exceptions.BusinessRuleException;
import ec.edu.ups.icc.events.core.exceptions.ForbiddenException;
import ec.edu.ups.icc.events.core.exceptions.ResourceNotFoundException;
import ec.edu.ups.icc.events.core.exceptions.UnauthorizedException;
import ec.edu.ups.icc.events.events.entities.EventEntity;
import ec.edu.ups.icc.events.events.entities.EventStatus;
import ec.edu.ups.icc.events.events.repositories.EventRepository;
import ec.edu.ups.icc.events.registrations.dtos.RegistrationDTO;
import ec.edu.ups.icc.events.registrations.entities.RegistrationEntity;
import ec.edu.ups.icc.events.registrations.entities.RegistrationStatus;
import ec.edu.ups.icc.events.registrations.repositories.RegistrationRepository;
import ec.edu.ups.icc.events.users.entities.UserEntity;
import ec.edu.ups.icc.events.users.repositories.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ec.edu.ups.icc.events.audit.annotations.Auditable;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Transactional
public class RegistrationService {

    private final RegistrationRepository registrationRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    public RegistrationService(RegistrationRepository registrationRepository,
                               EventRepository eventRepository,
                               UserRepository userRepository) {
        this.registrationRepository = registrationRepository;
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
    }

@Auditable(
        action = "REGISTER_EVENT",
        failureAction = "REGISTER_EVENT_FAILED",
        resourceName = "EVENT",
        resourceIdArg = 0,
        captureResultId = false
)
public RegistrationDTO registerUserToEvent(Long eventId) {
    UserEntity user = findCurrentUser();

    EventEntity event = eventRepository.findById(eventId)
            .orElseThrow(() ->
                    new ResourceNotFoundException(
                            "Evento no encontrado con id: " + eventId
                    )
            );

    if (event.getStatus() != EventStatus.PUBLISHED) {
        throw new BusinessRuleException(
                "No se permiten inscripciones en eventos que no estén publicados"
        );
    }

    if (LocalDateTime.now().isAfter(event.getStartDate())) {
        throw new BusinessRuleException(
                "No se permiten inscripciones en eventos que ya hayan iniciado o finalizado"
        );
    }

    Optional<RegistrationEntity> existingOpt =
            registrationRepository.findByUserIdAndEventId(
                    user.getId(),
                    event.getId()
            );

    if (existingOpt.isPresent()) {
        RegistrationEntity existing = existingOpt.get();

        if (existing.getStatus() == RegistrationStatus.CONFIRMED) {
            throw new BusinessRuleException(
                    "El usuario ya se encuentra inscrito de forma activa en este evento"
            );
        }

        if (event.getAvailableSeats() <= 0) {
            throw new BusinessRuleException(
                    "No hay cupos disponibles"
            );
        }

        event.setAvailableSeats(
                event.getAvailableSeats() - 1
        );
        eventRepository.save(event);

        existing.setStatus(
                RegistrationStatus.CONFIRMED
        );
        existing.setRegistrationDate(
                LocalDateTime.now()
        );

        return toDto(
                registrationRepository.save(existing)
        );
    }

    if (event.getAvailableSeats() <= 0) {
        throw new BusinessRuleException(
                "No hay cupos disponibles"
        );
    }

    event.setAvailableSeats(
            event.getAvailableSeats() - 1
    );
    eventRepository.save(event);

    RegistrationEntity registration =
            new RegistrationEntity();

    registration.setUser(user);
    registration.setEvent(event);
    registration.setStatus(
            RegistrationStatus.CONFIRMED
    );
    registration.setRegistrationDate(
            LocalDateTime.now()
    );

    return toDto(
            registrationRepository.save(registration)
    );
}

    public RegistrationDTO cancelRegistration(Long id) {
        UserEntity currentUser = findCurrentUser();
        RegistrationEntity registration = registrationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inscripción no encontrada con id: " + id));

        // Validación de propiedad (ownership)
        boolean isAdmin = currentUser.getRoles().stream()
                .anyMatch(r -> r.getName().equals("ROLE_ADMIN"));
        if (!isAdmin && !registration.getUser().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("No posee permisos para cancelar esta inscripción");
        }

        if (registration.getStatus() == RegistrationStatus.CANCELLED) {
            throw new BusinessRuleException("La inscripción ya se encuentra cancelada");
        }

        registration.setStatus(RegistrationStatus.CANCELLED);
        EventEntity event = registration.getEvent();
        event.setAvailableSeats(event.getAvailableSeats() + 1);
        eventRepository.save(event);

        return toDto(registrationRepository.save(registration));
    }

    @Transactional(readOnly = true)
    public Page<RegistrationDTO> getRegistrations(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        UserEntity currentUser = findCurrentUser();

        boolean isAdmin = currentUser.getRoles().stream()
                .anyMatch(r -> r.getName().equals("ROLE_ADMIN"));
        boolean isOrganizer = currentUser.getRoles().stream()
                .anyMatch(r -> r.getName().equals("ROLE_ORGANIZER"));

        Page<RegistrationEntity> entitiesPage;
        if (isAdmin) {
            entitiesPage = registrationRepository.findAll(pageable);
        } else if (isOrganizer) {
            entitiesPage = registrationRepository.findByEventOrganizerId(currentUser.getId(), pageable);
        } else {
            entitiesPage = registrationRepository.findByUserId(currentUser.getId(), pageable);
        }

        return entitiesPage.map(this::toDto);
    }

    private UserEntity findCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String email;
        if (principal instanceof org.springframework.security.core.userdetails.User userDetails) {
            email = userDetails.getUsername();
        } else {
            throw new UnauthorizedException("Usuario no autenticado");
        }
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("Usuario no encontrado en el sistema"));
    }

    private RegistrationDTO toDto(RegistrationEntity entity) {
        return new RegistrationDTO(
                entity.getId(),
                entity.getUser().getId(),
                entity.getUser().getEmail(),
                entity.getEvent().getId(),
                entity.getEvent().getTitle(),
                entity.getRegistrationDate(),
                entity.getStatus()
        );
    }
}
