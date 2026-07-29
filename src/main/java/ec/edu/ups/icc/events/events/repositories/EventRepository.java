package ec.edu.ups.icc.events.events.repositories;

import ec.edu.ups.icc.events.events.entities.EventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<EventEntity, Long>, JpaSpecificationExecutor<EventEntity> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from EventEntity e where e.id = :id")
    Optional<EventEntity> findByIdForUpdate(@Param("id") Long id);
}
