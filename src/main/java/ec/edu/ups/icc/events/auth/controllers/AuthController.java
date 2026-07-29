package ec.edu.ups.icc.events.auth.controllers;

import ec.edu.ups.icc.events.audit.annotations.Auditable;
import ec.edu.ups.icc.events.auth.dtos.AuthResponseDto;
import ec.edu.ups.icc.events.auth.dtos.LoginRequestDto;
import ec.edu.ups.icc.events.auth.dtos.RegisterRequestDto;
import ec.edu.ups.icc.events.auth.services.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDto> register(
            @Valid @RequestBody RegisterRequestDto request
    ) {
        AuthResponseDto response = authService.register(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    @Auditable(
            action = "LOGIN_SUCCESS",
            failureAction = "LOGIN_FAILED",
            resourceName = "AUTHENTICATION",
            captureResultId = false
    )
    public ResponseEntity<AuthResponseDto> login(
            @Valid @RequestBody LoginRequestDto request,
            HttpServletRequest httpServletRequest
    ) {
        String ip = resolveIpAddress(httpServletRequest);
        AuthResponseDto response = authService.login(request, ip);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponseDto> refresh(
            @RequestHeader("Authorization") String authHeader
    ) {
        AuthResponseDto response = authService.refresh(authHeader);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @RequestHeader("Authorization") String authHeader
    ) {
        authService.logout(authHeader);
        return ResponseEntity.ok().build();
    }

    private String resolveIpAddress(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
