package ec.edu.ups.icc.events.reports.services;

import ec.edu.ups.icc.events.core.exceptions.ForbiddenException;
import ec.edu.ups.icc.events.core.exceptions.ResourceNotFoundException;
import ec.edu.ups.icc.events.core.exceptions.UnauthorizedException;
import ec.edu.ups.icc.events.events.entities.EventEntity;
import ec.edu.ups.icc.events.events.repositories.EventRepository;
import ec.edu.ups.icc.events.registrations.entities.RegistrationEntity;
import ec.edu.ups.icc.events.registrations.repositories.RegistrationRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ReportAccessService {

    private final EventRepository eventRepository;
    private final RegistrationRepository registrationRepository;

    public ReportAccessService(
            EventRepository eventRepository,
            RegistrationRepository registrationRepository
    ) {
        this.eventRepository = eventRepository;
        this.registrationRepository = registrationRepository;
    }

    public void verifyEventReportAccess(Long eventId) {
        EventEntity event = eventRepository.findById(eventId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Evento no encontrado con id: " + eventId
                        )
                );

        Authentication authentication =
                getCurrentAuthentication();

        if (hasAuthority(authentication, "ROLE_ADMIN")) {
            return;
        }

        boolean isOrganizerOwner =
                hasAuthority(
                        authentication,
                        "ROLE_ORGANIZER"
                )
                && event.getOrganizer() != null
                && event.getOrganizer().getEmail() != null
                && event.getOrganizer()
                        .getEmail()
                        .equalsIgnoreCase(
                                authentication.getName()
                        );

        if (!isOrganizerOwner) {
            throw new ForbiddenException(
                    "No tienes permisos para descargar "
                            + "los inscritos de este evento"
            );
        }
    }

    public void verifyCertificateAccess(
            Long registrationId
    ) {
        RegistrationEntity registration =
                registrationRepository
                        .findById(registrationId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Inscripción no encontrada "
                                                + "con id: "
                                                + registrationId
                                )
                        );

        Authentication authentication =
                getCurrentAuthentication();

        if (hasAuthority(authentication, "ROLE_ADMIN")) {
            return;
        }

        boolean isParticipantOwner =
                hasAuthority(
                        authentication,
                        "ROLE_PARTICIPANT"
                )
                && registration.getUser() != null
                && registration.getUser().getEmail() != null
                && registration.getUser()
                        .getEmail()
                        .equalsIgnoreCase(
                                authentication.getName()
                        );

        if (!isParticipantOwner) {
            throw new ForbiddenException(
                    "No tienes permisos para descargar "
                            + "este comprobante de inscripción"
            );
        }
    }

    private Authentication getCurrentAuthentication() {
        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(
                        authentication.getPrincipal()
                )) {

            throw new UnauthorizedException(
                    "Usuario no autenticado"
            );
        }

        return authentication;
    }

    private boolean hasAuthority(
            Authentication authentication,
            String authority
    ) {
        return authentication
                .getAuthorities()
                .stream()
                .anyMatch(grantedAuthority ->
                        grantedAuthority
                                .getAuthority()
                                .equals(authority)
                );
    }
}