package ec.edu.ups.icc.events.audit.repositories;
import ec.edu.ups.icc.events.audit.entities.AuditLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditLogRepository
        extends JpaRepository<AuditLogEntity, Long> {
}