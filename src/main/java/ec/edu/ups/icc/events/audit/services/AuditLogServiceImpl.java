package ec.edu.ups.icc.events.audit.services;

import ec.edu.ups.icc.events.audit.entities.AuditLogEntity;
import ec.edu.ups.icc.events.audit.repositories.AuditLogRepository;
import ec.edu.ups.icc.events.audit.services.AuditLogService;
import ec.edu.ups.icc.events.users.entities.UserEntity;
import ec.edu.ups.icc.events.users.repositories.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
public class AuditLogServiceImpl implements AuditLogService{

    private static final int ACTION_MAX_LENGTH = 100;
    private static final int RESOURCE_NAME_MAX_LENGTH = 100;
    private static final int RESOURCE_ID_MAX_LENGTH = 50;
    private static final int IP_ADDRESS_MAX_LENGTH = 45;
    private static final int USER_AGENT_MAX_LENGTH = 500;

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    public AuditLogServiceImpl(
            AuditLogRepository auditLogRepository,
            UserRepository userRepository) {
        this.auditLogRepository = auditLogRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public AuditLogEntity record(
            String action,
            String resourceName,
            String resourceId,
            String details) {
        AuditLogEntity auditLog = new AuditLogEntity();

        auditLog.setUser(resolveAuthenticatedUser());
        auditLog.setAction(truncate(action, ACTION_MAX_LENGTH));
        auditLog.setResourceName(truncate(resourceName, RESOURCE_NAME_MAX_LENGTH));
        auditLog.setResourceId(truncate(resourceId, RESOURCE_ID_MAX_LENGTH));
        auditLog.setDetails(details);

        HttpServletRequest request = currentRequest();

        if (request != null) {
            auditLog.setIpAddress(truncate(resolveIpAddress(request), IP_ADDRESS_MAX_LENGTH));
            auditLog.setUserAgent(truncate(request.getHeader("User-Agent"), USER_AGENT_MAX_LENGTH));
        }

        return auditLogRepository.save(auditLog);
    }

    private UserEntity resolveAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return null;
        }

        String email = authentication.getName();

        if (email == null || email.isBlank()) {
            return null;
        }

        return userRepository
                .findByEmail(email.trim())
                .orElse(null);
    }

    private HttpServletRequest currentRequest() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            return attributes.getRequest();
        }

        return null;
    }

    private String resolveIpAddress(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");

        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }

        return request.getRemoteAddr();
    }

    private String truncate(String value, int maximumLength) {
        if (value == null) {
            return null;
        }

        String normalizedValue = value.trim();

        if (normalizedValue.length() <= maximumLength) {
            return normalizedValue;
        }

        return normalizedValue.substring(0, maximumLength);
    }
}
