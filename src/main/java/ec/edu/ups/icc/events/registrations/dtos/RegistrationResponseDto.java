package ec.edu.ups.icc.events.registrations.dtos;

import ec.edu.ups.icc.events.registrations.entities.RegistrationStatus;

import java.time.LocalDateTime;

public class RegistrationResponseDto {

    private Long id;
    private Long userId;
    private String userEmail;
    private Long eventId;
    private String eventTitle;
    private LocalDateTime registrationDate;
    private RegistrationStatus status;

    public RegistrationResponseDto() {
    }

    public RegistrationResponseDto(Long id, Long userId, String userEmail, Long eventId, String eventTitle, LocalDateTime registrationDate, RegistrationStatus status) {
        this.id = id;
        this.userId = userId;
        this.userEmail = userEmail;
        this.eventId = eventId;
        this.eventTitle = eventTitle;
        this.registrationDate = registrationDate;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public String getEventTitle() {
        return eventTitle;
    }

    public void setEventTitle(String eventTitle) {
        this.eventTitle = eventTitle;
    }

    public LocalDateTime getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(LocalDateTime registrationDate) {
        this.registrationDate = registrationDate;
    }

    public RegistrationStatus getStatus() {
        return status;
    }

    public void setStatus(RegistrationStatus status) {
        this.status = status;
    }
}
