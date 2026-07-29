package ec.edu.ups.icc.events.core.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final Environment environment;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter,
            Environment environment
    ) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.environment = environment;
    }

    /*
     * Cadena exclusiva para Swagger.
     * En desarrollo permite acceso libre.
     * En producción exige autenticación Basic.
     */
    @Bean
    @Order(1)
    public SecurityFilterChain swaggerSecurityFilterChain(
            HttpSecurity http
    ) throws Exception {

        boolean isProd = Arrays
                .asList(environment.getActiveProfiles())
                .contains("prod");

        http
                .securityMatcher(
                        "/swagger-ui.html",
                        "/swagger-ui/**",
                        "/v3/api-docs/**"
                )
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(authorize -> authorize
                        .anyRequest().permitAll()
                );

        if (isProd) {
            String swaggerUsername = firstNonBlank(
                    environment.getProperty("SWAGGER_USERNAME"),
                    environment.getProperty("swagger.user")
            );

            String swaggerPassword = firstNonBlank(
                    environment.getProperty("SWAGGER_PASSWORD"),
                    environment.getProperty("swagger.password")
            );

            if (swaggerUsername == null || swaggerPassword == null) {
                throw new IllegalStateException(
                        "SWAGGER_USERNAME y SWAGGER_PASSWORD "
                                + "son obligatorias en producción"
                );
            }

            String credentials =
                    swaggerUsername + ":" + swaggerPassword;

            String expectedAuthorization =
                    "Basic " + Base64.getEncoder().encodeToString(
                            credentials.getBytes(StandardCharsets.UTF_8)
                    );

            OncePerRequestFilter swaggerBasicAuthFilter =
                    new OncePerRequestFilter() {

                        @Override
                        protected void doFilterInternal(
                                HttpServletRequest request,
                                HttpServletResponse response,
                                FilterChain filterChain
                        ) throws ServletException, IOException {

                            String authorization = request.getHeader(
                                    HttpHeaders.AUTHORIZATION
                            );

                            boolean validCredentials =
                                    authorization != null
                                            && MessageDigest.isEqual(
                                                    authorization.getBytes(
                                                            StandardCharsets.UTF_8
                                                    ),
                                                    expectedAuthorization.getBytes(
                                                            StandardCharsets.UTF_8
                                                    )
                                            );

                            if (!validCredentials) {
                                response.setStatus(
                                        HttpServletResponse.SC_UNAUTHORIZED
                                );

                                response.setHeader(
                                        HttpHeaders.WWW_AUTHENTICATE,
                                        "Basic realm=\"Swagger Docs\""
                                );

                                response.setHeader(
                                        HttpHeaders.CACHE_CONTROL,
                                        "no-store"
                                );

                                response.setContentType(
                                        "application/json"
                                );

                                response.setCharacterEncoding(
                                        StandardCharsets.UTF_8.name()
                                );

                                response.getWriter().write(
                                        "{\"status\":401,"
                                                + "\"message\":"
                                                + "\"Credenciales de Swagger requeridas\"}"
                                );

                                return;
                            }

                            filterChain.doFilter(request, response);
                        }
                    };

            http.addFilterBefore(
                    swaggerBasicAuthFilter,
                    UsernamePasswordAuthenticationFilter.class
            );
        }

        return http.build();
    }

    /*
     * Cadena principal de la API con autenticación JWT.
     */
    @Bean
    @Order(2)
    public SecurityFilterChain apiSecurityFilterChain(
            HttpSecurity http
    ) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(
                                "/api/auth/**",
                                "/actuator/health",
                                "/api/actuator/health",
                                "/api/registrations/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    private String firstNonBlank(
            String first,
            String second
    ) {
        if (first != null && !first.isBlank()) {
            return first;
        }

        if (second != null && !second.isBlank()) {
            return second;
        }

        return null;
    }
}