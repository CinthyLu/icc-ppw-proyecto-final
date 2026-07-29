package ec.edu.ups.icc.events.registrations.services;

import ec.edu.ups.icc.events.audit.annotations.Auditable;
import ec.edu.ups.icc.events.core.exceptions.BusinessRuleException;
import ec.edu.ups.icc.events.core.exceptions.ForbiddenException;
import ec.edu.ups.icc.events.core.exceptions.ResourceNotFoundException;
import ec.edu.ups.icc.events.core.exceptions.UnauthorizedException;
import ec.edu.ups.icc.events.events.entities.EventEntity;
import ec.edu.ups.icc.events.events.entities.EventStatus;
import ec.edu.ups.icc.events.events.repositories.EventRepository;
import ec.edu.ups.icc.events.registrations.dtos.RegistrationResponseDto;
import ec.edu.ups.icc.events.registrations.entities.RegistrationEntity;
import ec.edu.ups.icc.events.registrations.entities.RegistrationStatus;
import ec.edu.ups.icc.events.registrations.mappers.RegistrationMapper;
import ec.edu.ups.icc.events.registrations.repositories.RegistrationRepository;
import ec.edu.ups.icc.events.registrations.services.RegistrationService;
import ec.edu.ups.icc.events.users.entities.UserEntity;
import ec.edu.ups.icc.events.users.repositories.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Transactional
public class RegistrationServiceImpl implements RegistrationService {

    private final RegistrationRepository registrationRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    public RegistrationServiceImpl(
            RegistrationRepository registrationRepository,
            EventRepository eventRepository,
            UserRepository userRepository
    ) {
        this.registrationRepository = registrationRepository;
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Auditable(
            action = "REGISTER_EVENT",
            failureAction = "REGISTER_EVENT_FAILED",
            resourceName = "EVENT",
            resourceIdArg = 0,
            captureResultId = false
    )
    public RegistrationResponseDto registerUserToEvent(Long eventId) {
        UserEntity user = findCurrentUser();

        EventEntity event = eventRepository.findByIdForUpdate(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Evento no encontrado con id: " + eventId));

        if (event.getStatus() != EventStatus.PUBLISHED) {
            throw new BusinessRuleException("No se permiten inscripciones en eventos que no estén publicados");
        }

        LocalDateTime now = LocalDateTime.now();
        if (event.getStartDate() != null && !now.isBefore(event.getStartDate())) {
            throw new BusinessRuleException("No se permiten inscripciones en eventos que ya hayan iniciado");
        }

        if (event.getEndDate() != null && !now.isBefore(event.getEndDate())) {
            throw new BusinessRuleException("No se permiten inscripciones en eventos finalizados");
        }

        Optional<RegistrationEntity> existingOpt = registrationRepository.findByUserIdAndEventId(user.getId(), event.getId());
        if (existingOpt.isPresent()) {
            throw new BusinessRuleException("El usuario ya se encuentra inscrito en este evento");
        }

        if (event.getAvailableSeats() == null || event.getAvailableSeats() <= 0) {
            throw new BusinessRuleException("No hay cupos disponibles");
        }

        event.setAvailableSeats(event.getAvailableSeats() - 1);
        eventRepository.save(event);

        RegistrationEntity registration = new RegistrationEntity();
        registration.setUser(user);
        registration.setEvent(event);
        registration.setStatus(RegistrationStatus.CONFIRMED);
        registration.setRegistrationDate(LocalDateTime.now());

        return RegistrationMapper.toResponse(registrationRepository.save(registration));
    }

    @Override
    public RegistrationResponseDto cancelRegistration(Long id) {
        UserEntity currentUser = findCurrentUser();
        RegistrationEntity registration = registrationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inscripción no encontrada con id: " + id));

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

        return RegistrationMapper.toResponse(registrationRepository.save(registration));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RegistrationResponseDto> getRegistrations(int page, int size, Long eventId, String status, String sortBy, String sortDir) {
        Sort sort = Sort.by(resolveSortDirection(sortDir), resolveSortProperty(sortBy));
        Pageable pageable = PageRequest.of(page, size, sort);
        UserEntity currentUser = findCurrentUser();

        boolean isAdmin = currentUser.getRoles().stream()
                .anyMatch(r -> r.getName().equals("ROLE_ADMIN"));
        boolean isOrganizer = currentUser.getRoles().stream()
                .anyMatch(r -> r.getName().equals("ROLE_ORGANIZER"));

        Specification<RegistrationEntity> spec = buildSpecification(eventId, status, currentUser, isAdmin, isOrganizer);
        Page<RegistrationEntity> entitiesPage = registrationRepository.findAll(spec, pageable);

        return entitiesPage.map(RegistrationMapper::toResponse);
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

    private Specification<RegistrationEntity> buildSpecification(Long eventId, String status, UserEntity currentUser, boolean isAdmin, boolean isOrganizer) {
        return (root, query, builder) -> {
            var predicates = builder.conjunction();

            if (eventId != null) {
                predicates = builder.and(predicates, builder.equal(root.get("event").get("id"), eventId));
            }

            if (status != null && !status.isBlank()) {
                predicates = builder.and(predicates, builder.equal(root.get("status"), RegistrationStatus.valueOf(status.toUpperCase())));
            }

            if (!isAdmin) {
                if (isOrganizer) {
                    predicates = builder.and(predicates, builder.equal(root.get("event").get("organizer").get("id"), currentUser.getId()));
                } else {
                    predicates = builder.and(predicates, builder.equal(root.get("user").get("id"), currentUser.getId()));
                }
            }

            return predicates;
        };
    }

    private Sort.Direction resolveSortDirection(String sortDir) {
        return "desc".equalsIgnoreCase(sortDir) ? Sort.Direction.DESC : Sort.Direction.ASC;
    }

    private String resolveSortProperty(String sortBy) {
        return switch (sortBy != null ? sortBy.toLowerCase() : "") {
            case "event" -> "event";
            case "status" -> "status";
            case "date" -> "registrationDate";
            default -> "registrationDate";
        };
    }
}
