package ec.edu.ups.icc.events.audit.services;

import ec.edu.ups.icc.events.audit.entities.AuditLogEntity;


public interface AuditLogService {
    AuditLogEntity record(String action, String resourceName, String resourceId, String details);
}