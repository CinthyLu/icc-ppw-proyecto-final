package ec.edu.ups.icc.events.events.dtos;

import ec.edu.ups.icc.events.events.entities.EventModality;
import ec.edu.ups.icc.events.events.entities.EventStatus;

import java.time.LocalDateTime;

public class EventResponseDto {

    private Long id;
    private String title;
    private String description;
    private EventModality modality;
    private String location;
    private Integer capacity;
    private Integer availableSeats;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private EventStatus status;
    private Long organizerId;
    private Long categoryId;

    public EventResponseDto() {
    }

    public EventResponseDto(Long id, String title, String description, EventModality modality, String location, Integer capacity, Integer availableSeats, LocalDateTime startDate, LocalDateTime endDate, EventStatus status, Long organizerId, Long categoryId) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.modality = modality;
        this.location = location;
        this.capacity = capacity;
        this.availableSeats = availableSeats;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.organizerId = organizerId;
        this.categoryId = categoryId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public EventModality getModality() {
        return modality;
    }

    public void setModality(EventModality modality) {
        this.modality = modality;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public Integer getAvailableSeats() {
        return availableSeats;
    }

    public void setAvailableSeats(Integer availableSeats) {
        this.availableSeats = availableSeats;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public EventStatus getStatus() {
        return status;
    }

    public void setStatus(EventStatus status) {
        this.status = status;
    }

    public Long getOrganizerId() {
        return organizerId;
    }

    public void setOrganizerId(Long organizerId) {
        this.organizerId = organizerId;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }
}
