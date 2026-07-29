package ec.edu.ups.icc.events.audit.services;

import ec.edu.ups.icc.events.audit.entities.AuditLogEntity;
import ec.edu.ups.icc.events.audit.repositories.AuditLogRepository;
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

public interface AuditLogService {
    AuditLogEntity record(String action, String resourceName, String resourceId, String details);
}