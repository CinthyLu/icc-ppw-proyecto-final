package ec.edu.ups.icc.events.events.services;

import ec.edu.ups.icc.events.categories.entities.CategoryEntity;
import ec.edu.ups.icc.events.categories.repositories.CategoryRepository;
import ec.edu.ups.icc.events.core.exceptions.ForbiddenException;
import ec.edu.ups.icc.events.core.exceptions.ResourceNotFoundException;
import ec.edu.ups.icc.events.core.exceptions.UnauthorizedException;
import ec.edu.ups.icc.events.events.dtos.EventDTO;
import ec.edu.ups.icc.events.events.dtos.EventFilterDTO;
import ec.edu.ups.icc.events.events.entities.EventEntity;
import ec.edu.ups.icc.events.events.entities.EventStatus;
import ec.edu.ups.icc.events.events.repositories.EventRepository;
import ec.edu.ups.icc.events.users.entities.UserEntity;
import ec.edu.ups.icc.events.users.repositories.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ec.edu.ups.icc.events.audit.annotations.Auditable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class EventService {

    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    public EventService(EventRepository eventRepository,
            CategoryRepository categoryRepository,
            UserRepository userRepository) {
        this.eventRepository = eventRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
    }

    public Page<EventDTO> searchEvents(EventFilterDTO filter, int page, int size, String sortBy, String sortDir) {
        Sort sort = Sort.by(resolveSortDirection(sortDir), resolveSortProperty(sortBy));
        Pageable pageable = PageRequest.of(page, size, sort);
        Specification<EventEntity> spec = buildSpecification(filter);
        Page<EventEntity> results = eventRepository.findAll(spec, pageable);
        List<EventDTO> content = results.stream().map(this::toDto).collect(Collectors.toList());
        return new PageImpl<>(content, pageable, results.getTotalElements());
    }

    public EventDTO getEventById(Long id) {
        return eventRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Evento no encontrado con id: " + id));
    }

    @Auditable(
        action = "CREATE_EVENT",
        failureAction = "CREATE_EVENT_FAILED",
        resourceName = "EVENT"
    )
    public EventDTO createEvent(EventDTO dto) {
        EventEntity event = new EventEntity();
        event.setTitle(dto.title());
        event.setDescription(dto.description());
        event.setModality(dto.modality());
        event.setLocation(dto.location());
        event.setCapacity(dto.capacity());
        event.setAvailableSeats(dto.availableSeats());
        event.setStartDate(dto.startDate());
        event.setEndDate(dto.endDate());
        event.setStatus(dto.status());
        event.setCategory(findCategory(dto.categoryId()));
        event.setOrganizer(findCurrentUser());

        return toDto(eventRepository.save(event));
    }



    public EventDTO updateEvent(Long id, EventDTO dto) {
        EventEntity event = findEventById(id);
        verifyOwnership(event);

        if (dto.title() != null)
            event.setTitle(dto.title());
        if (dto.description() != null)
            event.setDescription(dto.description());
        if (dto.modality() != null)
            event.setModality(dto.modality());
        if (dto.location() != null)
            event.setLocation(dto.location());
        if (dto.capacity() != null)
            event.setCapacity(dto.capacity());
        if (dto.availableSeats() != null)
            event.setAvailableSeats(dto.availableSeats());
        if (dto.startDate() != null)
            event.setStartDate(dto.startDate());
        if (dto.endDate() != null)
            event.setEndDate(dto.endDate());
        if (dto.status() != null)
            event.setStatus(dto.status());
        if (dto.categoryId() != null)
            event.setCategory(findCategory(dto.categoryId()));

        return toDto(eventRepository.save(event));
    }

    public void deleteEvent(Long id) {
        EventEntity event = findEventById(id);
        verifyOwnership(event);
        event.setStatus(EventStatus.CANCELLED);
        eventRepository.save(event);
    }

    private EventEntity findEventById(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Evento no encontrado con id: " + id));
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

    private void verifyOwnership(EventEntity event) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String email;
        if (principal instanceof org.springframework.security.core.userdetails.User userDetails) {
            email = userDetails.getUsername();
        } else {
            throw new UnauthorizedException("Usuario no autenticado");
        }

        boolean isAdmin = SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin && !event.getOrganizer().getEmail().equals(email)) {
            throw new ForbiddenException("No tienes permisos para modificar o eliminar este evento");
        }
    }

    private CategoryEntity findCategory(Long id) {
        return Optional.ofNullable(id)
                .flatMap(categoryRepository::findById)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con id: " + id));
    }

    private Specification<EventEntity> buildSpecification(EventFilterDTO filter) {
        return (root, query, builder) -> {
            var predicates = builder.conjunction();

            predicates = builder.and(predicates, builder.notEqual(root.get("status"), EventStatus.CANCELLED));

            if (filter.text() != null && !filter.text().isBlank()) {
                String pattern = "%" + filter.text().toLowerCase() + "%";
                predicates = builder.and(predicates,
                        builder.or(
                                builder.like(builder.lower(root.get("title")), pattern),
                                builder.like(builder.lower(root.get("description")), pattern),
                                builder.like(builder.lower(root.get("location")), pattern)));
            }
            if (filter.categoryId() != null) {
                predicates = builder.and(predicates,
                        builder.equal(root.get("category").get("id"), filter.categoryId()));
            }
            if (filter.modality() != null) {
                predicates = builder.and(predicates,
                        builder.equal(root.get("modality"), filter.modality()));
            }
            if (filter.startDate() != null) {
                predicates = builder.and(predicates,
                        builder.greaterThanOrEqualTo(root.get("startDate"), filter.startDate()));
            }
            if (filter.endDate() != null) {
                predicates = builder.and(predicates,
                        builder.lessThanOrEqualTo(root.get("endDate"), filter.endDate()));
            }
            return predicates;
        };
    }

    private Sort.Direction resolveSortDirection(String sortDir) {
        return "desc".equalsIgnoreCase(sortDir) ? Sort.Direction.DESC : Sort.Direction.ASC;
    }

    private String resolveSortProperty(String sortBy) {
        return switch (sortBy != null ? sortBy.toLowerCase() : "") {
            case "title" -> "title";
            case "startdate" -> "startDate";
            case "enddate" -> "endDate";
            case "createdat" -> "createdAt";
            default -> "startDate";
        };
    }

    private EventDTO toDto(EventEntity event) {
        return new EventDTO(
                event.getId(),
                event.getTitle(),
                event.getDescription(),
                event.getModality(),
                event.getLocation(),
                event.getCapacity(),
                event.getAvailableSeats(),
                event.getStartDate(),
                event.getEndDate(),
                event.getStatus(),
                event.getOrganizer() != null ? event.getOrganizer().getId() : null,
                event.getCategory() != null ? event.getCategory().getId() : null);
    }
}
