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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticación", description = "Endpoints para registro, login, refresh token y logout de usuarios")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @Operation(summary = "Registrar un nuevo participante", description = "Permite registrar un nuevo usuario con rol de PARTICIPANTE (público).")
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
    @Operation(summary = "Iniciar sesión", description = "Autentica al usuario devolviendo un Token JWT de acceso y un Refresh Token. (Bloqueo temporal tras 5 fallos).")
    public ResponseEntity<AuthResponseDto> login(
            @Valid @RequestBody LoginRequestDto request,
            HttpServletRequest httpServletRequest
    ) {
        String ip = resolveIpAddress(httpServletRequest);
        AuthResponseDto response = authService.login(request, ip);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refrescar Access Token", description = "Genera un nuevo par de Access Token y Refresh Token a partir de un Refresh Token válido.")
    public ResponseEntity<AuthResponseDto> refresh(
            @RequestHeader("Authorization") String authHeader
    ) {
        AuthResponseDto response = authService.refresh(authHeader);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    @Operation(summary = "Cerrar sesión", description = "Invalida el Access Token actual agregándolo a una lista negra en Redis.", security = @SecurityRequirement(name = "bearerAuth"))
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
