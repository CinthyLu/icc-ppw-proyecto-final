package ec.edu.ups.icc.events.auth.services;

import ec.edu.ups.icc.events.auth.dtos.AuthResponseDto;
import ec.edu.ups.icc.events.auth.dtos.LoginRequestDto;
import ec.edu.ups.icc.events.auth.dtos.RegisterRequestDto;
import ec.edu.ups.icc.events.auth.mappers.AuthMapper;
import ec.edu.ups.icc.events.users.entities.UserEntity;
import ec.edu.ups.icc.events.users.entities.RoleEntity;
import ec.edu.ups.icc.events.users.repositories.UserRepository;
import ec.edu.ups.icc.events.users.repositories.RoleRepository;
import ec.edu.ups.icc.events.core.exceptions.BadRequestException;
import ec.edu.ups.icc.events.core.exceptions.ForbiddenException;
import ec.edu.ups.icc.events.core.exceptions.ResourceNotFoundException;
import ec.edu.ups.icc.events.security.services.JwtService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Collections;

@Service
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final StringRedisTemplate redisTemplate;
    private final ec.edu.ups.icc.events.security.config.RateLimitingProperties rateProps;

    public AuthServiceImpl(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            AuthenticationManager authenticationManager,
            StringRedisTemplate redisTemplate,
            ec.edu.ups.icc.events.security.config.RateLimitingProperties rateProps
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.redisTemplate = redisTemplate;
        this.rateProps = rateProps;
    }

    @Override
    public AuthResponseDto register(RegisterRequestDto dto) {
        String email = dto.getUsername().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new BadRequestException("El correo electrónico ya está registrado.");
        }

        UserEntity user = AuthMapper.toEntity(dto);
        user.setPassword(passwordEncoder.encode(dto.getPassword()));

        RoleEntity role = roleRepository.findByName("ROLE_PARTICIPANT")
                .orElseThrow(() -> new ResourceNotFoundException("El rol ROLE_PARTICIPANT no existe en el sistema."));

        user.setRoles(Collections.singleton(role));
        userRepository.save(user);

        String token = jwtService.generateAccessToken(user.getEmail());
        String refreshToken = jwtService.generateRefreshToken(user.getEmail());
        return new AuthResponseDto(token, refreshToken);
    }

    @Override
    public AuthResponseDto login(LoginRequestDto dto, String ipAddress) {
        String email = dto.getUsername().trim().toLowerCase();
        String blockKey = "blocked-user:" + ipAddress + ":" + email;

        if (Boolean.TRUE.equals(redisTemplate.hasKey(blockKey))) {
            throw new ec.edu.ups.icc.events.core.exceptions.AccountLockedException("Su usuario está bloqueado temporalmente en esta dirección IP debido a múltiples intentos fallidos de inicio de sesión.");
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, dto.getPassword())
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Limpiar intentos fallidos al tener éxito
            String attemptsKey = "login:failed-attempts:" + ipAddress + ":" + email;
            redisTemplate.delete(attemptsKey);

            String token = jwtService.generateAccessToken(authentication.getName());
            String refreshToken = jwtService.generateRefreshToken(authentication.getName());
            return new AuthResponseDto(token, refreshToken);
        } catch (AuthenticationException ex) {
            // Incrementar intentos fallidos en caso de error
            String attemptsKey = "login:failed-attempts:" + ipAddress + ":" + email;
            Long attempts = redisTemplate.opsForValue().increment(attemptsKey);
            if (attempts != null && attempts == 1) {
                redisTemplate.expire(attemptsKey, Duration.ofSeconds(rateProps.getLoginWindowSeconds()));
            }
            if (attempts != null && attempts >= rateProps.getLoginFailedToBlock()) {
                redisTemplate.opsForValue().set(blockKey, "true", Duration.ofMinutes(rateProps.getLoginBlockDurationMinutes()));
                redisTemplate.delete(attemptsKey);

                // Bloquear en base de datos si el usuario existe
                userRepository.findByEmail(email).ifPresent(u -> {
                    u.setAccountLocked(true);
                    userRepository.save(u);
                });

                throw new ec.edu.ups.icc.events.core.exceptions.AccountLockedException("Usuario bloqueado temporalmente por " + rateProps.getLoginBlockDurationMinutes() + " minutos debido a " + rateProps.getLoginFailedToBlock() + " intentos fallidos.");
            }
            throw new BadRequestException("Credenciales de inicio de sesión inválidas.");
        }
    }

    @Override
    public AuthResponseDto refresh(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new BadRequestException("Refresh token inválido");
        }

        String refreshToken = authHeader.substring(7);
        String username = jwtService.extractUsername(refreshToken);

        if (username != null && jwtService.isTokenValid(refreshToken, username)) {
            String newAccessToken = jwtService.generateAccessToken(username);
            String newRefreshToken = jwtService.generateRefreshToken(username);
            return new AuthResponseDto(newAccessToken, newRefreshToken);
        }

        throw new ForbiddenException("Token expirado o inválido");
    }

    @Override
    public void logout(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            redisTemplate.opsForValue().set(
                    token,
                    "blacklisted",
                    Duration.ofMinutes(15)
            );
        } else {
            throw new BadRequestException("Token de logout inválido");
        }
    }
}
