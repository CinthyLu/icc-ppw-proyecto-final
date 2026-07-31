package ec.edu.ups.icc.events.reports.services;

import java.time.LocalDateTime;
import ec.edu.ups.icc.events.reports.dtos.SystemStatisticsDto;

public interface StatisticsService {

    SystemStatisticsDto getSystemStatistics(LocalDateTime startDate, LocalDateTime endDate);
}