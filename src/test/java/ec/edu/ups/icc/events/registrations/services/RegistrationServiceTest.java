package ec.edu.ups.icc.events.registrations.services;

import ec.edu.ups.icc.events.core.exceptions.BusinessRuleException;
import ec.edu.ups.icc.events.core.exceptions.ResourceNotFoundException;
import ec.edu.ups.icc.events.events.entities.EventEntity;
import ec.edu.ups.icc.events.events.entities.EventStatus;
import ec.edu.ups.icc.events.events.repositories.EventRepository;
import ec.edu.ups.icc.events.registrations.dtos.RegistrationDTO;
import ec.edu.ups.icc.events.registrations.entities.RegistrationEntity;
import ec.edu.ups.icc.events.registrations.entities.RegistrationStatus;
import ec.edu.ups.icc.events.registrations.repositories.RegistrationRepository;
import ec.edu.ups.icc.events.users.entities.UserEntity;
import ec.edu.ups.icc.events.users.repositories.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RegistrationServiceTest {

    @Mock
    private RegistrationRepository registrationRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RegistrationService registrationService;

    private UserEntity userEntity;
    private EventEntity eventEntity;

    @BeforeEach
    public void setUp() {
        userEntity = new UserEntity();
        userEntity.setId(1L);
        userEntity.setEmail("student@ups.edu.ec");

        eventEntity = new EventEntity();
        eventEntity.setId(10L);
        eventEntity.setTitle("Event Test");
        eventEntity.setStatus(EventStatus.PUBLISHED);
        eventEntity.setCapacity(100);
        eventEntity.setAvailableSeats(10);
        eventEntity.setStartDate(LocalDateTime.now().plusDays(2));
        eventEntity.setEndDate(LocalDateTime.now().plusDays(3));
    }

    @AfterEach
    public void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void mockSecurityContext(String email) {
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        User springUser = new User(email, "password", Collections.singletonList(new SimpleGrantedAuthority("ROLE_PARTICIPANT")));
        when(authentication.getPrincipal()).thenReturn(springUser);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    public void registerUserToEvent_Success() {
        mockSecurityContext("student@ups.edu.ec");

        when(userRepository.findByEmail("student@ups.edu.ec")).thenReturn(Optional.of(userEntity));
        when(eventRepository.findById(10L)).thenReturn(Optional.of(eventEntity));
        when(registrationRepository.findByUserIdAndEventId(1L, 10L)).thenReturn(Optional.empty());

        RegistrationEntity savedRegistration = new RegistrationEntity();
        savedRegistration.setId(100L);
        savedRegistration.setUser(userEntity);
        savedRegistration.setEvent(eventEntity);
        savedRegistration.setStatus(RegistrationStatus.CONFIRMED);
        savedRegistration.setRegistrationDate(LocalDateTime.now());

        when(registrationRepository.save(any(RegistrationEntity.class))).thenReturn(savedRegistration);

        RegistrationDTO result = registrationService.registerUserToEvent(10L);

        assertNotNull(result);
        assertEquals(RegistrationStatus.CONFIRMED, result.status());
        assertEquals(9, eventEntity.getAvailableSeats());
        verify(eventRepository, times(1)).save(eventEntity);
        verify(registrationRepository, times(1)).save(any(RegistrationEntity.class));
    }

    @Test
    public void registerUserToEvent_EventNotFound() {
        mockSecurityContext("student@ups.edu.ec");

        when(userRepository.findByEmail("student@ups.edu.ec")).thenReturn(Optional.of(userEntity));
        when(eventRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            registrationService.registerUserToEvent(10L);
        });
    }

    @Test
    public void registerUserToEvent_EventNotPublished() {
        mockSecurityContext("student@ups.edu.ec");
        eventEntity.setStatus(EventStatus.DRAFT);

        when(userRepository.findByEmail("student@ups.edu.ec")).thenReturn(Optional.of(userEntity));
        when(eventRepository.findById(10L)).thenReturn(Optional.of(eventEntity));

        assertThrows(BusinessRuleException.class, () -> {
            registrationService.registerUserToEvent(10L);
        });
    }

    @Test
    public void registerUserToEvent_EventDatePassed() {
        mockSecurityContext("student@ups.edu.ec");
        eventEntity.setStartDate(LocalDateTime.now().minusDays(1));

        when(userRepository.findByEmail("student@ups.edu.ec")).thenReturn(Optional.of(userEntity));
        when(eventRepository.findById(10L)).thenReturn(Optional.of(eventEntity));

        assertThrows(BusinessRuleException.class, () -> {
            registrationService.registerUserToEvent(10L);
        });
    }

    @Test
    public void registerUserToEvent_NoSeats() {
        mockSecurityContext("student@ups.edu.ec");
        eventEntity.setAvailableSeats(0);

        when(userRepository.findByEmail("student@ups.edu.ec")).thenReturn(Optional.of(userEntity));
        when(eventRepository.findById(10L)).thenReturn(Optional.of(eventEntity));
        when(registrationRepository.findByUserIdAndEventId(1L, 10L)).thenReturn(Optional.empty());

        assertThrows(BusinessRuleException.class, () -> {
            registrationService.registerUserToEvent(10L);
        });
    }

    @Test
    public void registerUserToEvent_AlreadyRegisteredActive() {
        mockSecurityContext("student@ups.edu.ec");

        RegistrationEntity existingRegistration = new RegistrationEntity();
        existingRegistration.setUser(userEntity);
        existingRegistration.setEvent(eventEntity);
        existingRegistration.setStatus(RegistrationStatus.CONFIRMED);

        when(userRepository.findByEmail("student@ups.edu.ec")).thenReturn(Optional.of(userEntity));
        when(eventRepository.findById(10L)).thenReturn(Optional.of(eventEntity));
        when(registrationRepository.findByUserIdAndEventId(1L, 10L)).thenReturn(Optional.of(existingRegistration));

        assertThrows(BusinessRuleException.class, () -> {
            registrationService.registerUserToEvent(10L);
        });
    }

    @Test
    public void registerUserToEvent_Reactivation() {
        mockSecurityContext("student@ups.edu.ec");

        RegistrationEntity existingRegistration = new RegistrationEntity();
        existingRegistration.setId(100L);
        existingRegistration.setUser(userEntity);
        existingRegistration.setEvent(eventEntity);
        existingRegistration.setStatus(RegistrationStatus.CANCELLED);

        when(userRepository.findByEmail("student@ups.edu.ec")).thenReturn(Optional.of(userEntity));
        when(eventRepository.findById(10L)).thenReturn(Optional.of(eventEntity));
        when(registrationRepository.findByUserIdAndEventId(1L, 10L)).thenReturn(Optional.of(existingRegistration));
        when(registrationRepository.save(existingRegistration)).thenReturn(existingRegistration);

        RegistrationDTO result = registrationService.registerUserToEvent(10L);

        assertNotNull(result);
        assertEquals(RegistrationStatus.CONFIRMED, result.status());
        assertEquals(9, eventEntity.getAvailableSeats());
        verify(eventRepository, times(1)).save(eventEntity);
        verify(registrationRepository, times(1)).save(existingRegistration);
    }

    @Test
    public void cancelRegistration_Success() {
        mockSecurityContext("student@ups.edu.ec");

        RegistrationEntity registration = new RegistrationEntity();
        registration.setId(100L);
        registration.setUser(userEntity);
        registration.setEvent(eventEntity);
        registration.setStatus(RegistrationStatus.CONFIRMED);

        when(userRepository.findByEmail("student@ups.edu.ec")).thenReturn(Optional.of(userEntity));
        when(registrationRepository.findById(100L)).thenReturn(Optional.of(registration));
        when(registrationRepository.save(registration)).thenReturn(registration);

        RegistrationDTO result = registrationService.cancelRegistration(100L);

        assertNotNull(result);
        assertEquals(RegistrationStatus.CANCELLED, result.status());
        assertEquals(11, eventEntity.getAvailableSeats());
        verify(eventRepository, times(1)).save(eventEntity);
        verify(registrationRepository, times(1)).save(registration);
    }
}
