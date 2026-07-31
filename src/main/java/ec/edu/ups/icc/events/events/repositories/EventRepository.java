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


    long countByCreatedAtBetween(java.time.LocalDateTime start, java.time.LocalDateTime end);
    
    long countByModalityAndCreatedAtBetween(ec.edu.ups.icc.events.events.entities.EventModality modality, java.time.LocalDateTime start, java.time.LocalDateTime end);

    @Query("SELECT SUM(e.capacity) FROM EventEntity e WHERE e.createdAt BETWEEN :start AND :end")
    Integer sumCapacityByCreatedAtBetween(@Param("start") java.time.LocalDateTime start, @Param("end") java.time.LocalDateTime end);

    @Query("SELECT SUM(e.availableSeats) FROM EventEntity e WHERE e.createdAt BETWEEN :start AND :end")
    Integer sumAvailableSeatsByCreatedAtBetween(@Param("start") java.time.LocalDateTime start, @Param("end") java.time.LocalDateTime end);
}