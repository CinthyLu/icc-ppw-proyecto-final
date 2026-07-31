package ec.edu.ups.icc.events.reports.services;

import ec.edu.ups.icc.events.events.entities.EventModality;
import ec.edu.ups.icc.events.events.repositories.EventRepository;
import ec.edu.ups.icc.events.registrations.entities.RegistrationStatus;
import ec.edu.ups.icc.events.registrations.repositories.RegistrationRepository;
import ec.edu.ups.icc.events.reports.dtos.SystemStatisticsDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class StatisticsServiceImpl implements StatisticsService {

    private final EventRepository eventRepository;
    private final RegistrationRepository registrationRepository;

    public StatisticsServiceImpl(EventRepository eventRepository, RegistrationRepository registrationRepository) {
        this.eventRepository = eventRepository;
        this.registrationRepository = registrationRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public SystemStatisticsDto getSystemStatistics(LocalDateTime startDate, LocalDateTime endDate) {
        LocalDateTime start = startDate != null ? startDate : LocalDateTime.of(1970, 1, 1, 0, 0);
        LocalDateTime end = endDate != null ? endDate : LocalDateTime.now().plusYears(100);

        long totalEvents = eventRepository.countByCreatedAtBetween(start, end);
        long totalRegistrations = registrationRepository.countByRegistrationDateBetween(start, end);

        Map<String, Long> eventsByModality = new HashMap<>();
        for (EventModality modality : EventModality.values()) {
            long count = eventRepository.countByModalityAndCreatedAtBetween(modality, start, end);
            eventsByModality.put(modality.name(), count);
        }

        Map<String, Long> registrationsByStatus = new HashMap<>();
        for (RegistrationStatus status : RegistrationStatus.values()) {
            long count = registrationRepository.countByStatusAndRegistrationDateBetween(status, start, end);
            registrationsByStatus.put(status.name(), count);
        }

        Integer totalCapacity = eventRepository.sumCapacityByCreatedAtBetween(start, end);
        Integer availableSeats = eventRepository.sumAvailableSeatsByCreatedAtBetween(start, end);

        SystemStatisticsDto dto = new SystemStatisticsDto();
        dto.setTotalEvents(totalEvents);
        dto.setTotalRegistrations(totalRegistrations);
        dto.setEventsByModality(eventsByModality);
        dto.setRegistrationsByStatus(registrationsByStatus);
        dto.setTotalCapacity(totalCapacity != null ? totalCapacity : 0);
        dto.setAvailableSeats(availableSeats != null ? availableSeats : 0);

        return dto;
    }
}
