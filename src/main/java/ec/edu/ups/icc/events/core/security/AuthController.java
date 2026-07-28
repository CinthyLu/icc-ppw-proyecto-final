package ec.edu.ups.icc.events.core.security;

import ec.edu.ups.icc.events.audit.annotations.Auditable;
import ec.edu.ups.icc.events.users.entities.UserEntity;
import ec.edu.ups.icc.events.users.repositories.UserRepository;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final StringRedisTemplate redisTemplate;
    private final UserRepository userRepository;

    public AuthController(
            JwtService jwtService,
            AuthenticationManager authenticationManager,
            PasswordEncoder passwordEncoder,
            StringRedisTemplate redisTemplate,
            UserRepository userRepository
    ) {
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
        this.redisTemplate = redisTemplate;
        this.userRepository = userRepository;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(
            @RequestBody RegisterRequest request
    ) {
        String normalizedEmail = request.getUsername().trim().toLowerCase(Locale.ROOT);
        if (userRepository.existsByEmail(normalizedEmail)) {
            return ResponseEntity
                    .badRequest()
                    .body("El correo ya está registrado");
        }

        UserEntity user = new UserEntity();
        user.setName(normalizedEmail);
        user.setEmail(normalizedEmail);
        user.setPassword(
                passwordEncoder.encode(request.getPassword())
        );

        userRepository.save(user);

        return ResponseEntity.ok(
                "Participante registrado con éxito"
        );
    }

    @PostMapping("/login")
    @Auditable(
            action = "LOGIN_SUCCESS",
            failureAction = "LOGIN_FAILED",
            resourceName = "AUTHENTICATION",
            captureResultId = false
    )
    public ResponseEntity<?> login(
            @RequestBody LoginRequest request
    ) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        String accessToken =
                jwtService.generateAccessToken(
                        request.getUsername()
                );

        String refreshToken =
                jwtService.generateRefreshToken(
                        request.getUsername()
                );

        return ResponseEntity.ok(
                Map.of(
                        "accessToken", accessToken,
                        "refreshToken", refreshToken
                )
        );
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(
            @RequestHeader("Authorization") String authHeader
    ) {
        if (authHeader == null
                || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity
                    .badRequest()
                    .body("Refresh token inválido");
        }

        String refreshToken = authHeader.substring(7);
        String username =
                jwtService.extractUsername(refreshToken);

        if (jwtService.isTokenValid(
                refreshToken,
                username
        )) {
            String newAccessToken =
                    jwtService.generateAccessToken(username);

            return ResponseEntity.ok(
                    Map.of(
                            "accessToken",
                            newAccessToken
                    )
            );
        }

        return ResponseEntity
                .status(403)
                .body("Token expirado o inválido");
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(
            @RequestHeader("Authorization") String authHeader
    ) {
        if (authHeader != null
                && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            redisTemplate.opsForValue().set(
                    token,
                    "blacklisted",
                    15,
                    TimeUnit.MINUTES
            );

            return ResponseEntity.ok(
                    "Logout exitoso, token revocado"
            );
        }

        return ResponseEntity.badRequest().build();
    }
}

class RegisterRequest {

    private String username;
    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

class LoginRequest {

    private String username;
    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}