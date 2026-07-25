package ec.edu.ups.icc.events.sessions.repositories;

import ec.edu.ups.icc.events.sessions.entities.SessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SessionRepository extends JpaRepository<SessionEntity, Long> {
    List<SessionEntity> findByEventId(Long eventId);
}
