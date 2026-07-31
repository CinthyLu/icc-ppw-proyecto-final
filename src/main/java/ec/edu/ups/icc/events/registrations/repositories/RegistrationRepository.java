package ec.edu.ups.icc.events.registrations.repositories;

import ec.edu.ups.icc.events.registrations.entities.RegistrationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RegistrationRepository
        extends JpaRepository<RegistrationEntity, Long>,
        JpaSpecificationExecutor<RegistrationEntity> {

    boolean existsByEventId(Long eventId);

    Optional<RegistrationEntity> findByUserIdAndEventId(
            Long userId,
            Long eventId
    );

    Page<RegistrationEntity> findByUserId(
            Long userId,
            Pageable pageable
    );

    Page<RegistrationEntity> findByEventOrganizerId(
            Long organizerId,
            Pageable pageable
    );

    Page<RegistrationEntity> findAll(
            Specification<RegistrationEntity> spec,
            Pageable pageable
    );

    List<RegistrationEntity>
            findByEventIdOrderByRegistrationDateAsc(Long eventId);


    long countByRegistrationDateBetween(java.time.LocalDateTime start, java.time.LocalDateTime end);

    long countByStatusAndRegistrationDateBetween(
            ec.edu.ups.icc.events.registrations.entities.RegistrationStatus status,
            java.time.LocalDateTime start,
            java.time.LocalDateTime end
    );


        }