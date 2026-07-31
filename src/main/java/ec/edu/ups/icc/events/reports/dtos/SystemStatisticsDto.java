package ec.edu.ups.icc.events.reports.dtos;

import java.util.Map;

public class SystemStatisticsDto {
    private long totalEvents;
    private long totalRegistrations;
    private Map<String, Long> eventsByModality;
    private Map<String, Long> registrationsByStatus;
    private int totalCapacity;
    private int availableSeats;

    // Getters y Setters
    public long getTotalEvents() {
        return totalEvents;
    }

    public void setTotalEvents(long totalEvents) {
        this.totalEvents = totalEvents;
    }

    public long getTotalRegistrations() {
        return totalRegistrations;
    }

    public void setTotalRegistrations(long totalRegistrations) {
        this.totalRegistrations = totalRegistrations;
    }

    public Map<String, Long> getEventsByModality() {
        return eventsByModality;
    }

    public void setEventsByModality(Map<String, Long> eventsByModality) {
        this.eventsByModality = eventsByModality;
    }

    public Map<String, Long> getRegistrationsByStatus() {
        return registrationsByStatus;
    }

    public void setRegistrationsByStatus(Map<String, Long> registrationsByStatus) {
        this.registrationsByStatus = registrationsByStatus;
    }

    public int getTotalCapacity() {
        return totalCapacity;
    }

    public void setTotalCapacity(int totalCapacity) {
        this.totalCapacity = totalCapacity;
    }

    public int getAvailableSeats() {
        return availableSeats;
    }

    public void setAvailableSeats(int availableSeats) {
        this.availableSeats = availableSeats;
    }
}
