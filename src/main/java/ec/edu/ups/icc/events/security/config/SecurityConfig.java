package ec.edu.ups.icc.events.security.config;

import ec.edu.ups.icc.events.security.filters.JwtAuthenticationFilter;
import ec.edu.ups.icc.events.security.filters.RateLimitingFilter;
import org.springframework.http.HttpMethod;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final RateLimitingFilter rateLimitingFilter;

    @Value("${spring.profiles.active:dev}")
    private String activeProfile;

    @Value("${cors.allowed-origins:http://localhost:3000,http://localhost:8080}")
    private String allowedOrigins;

    @Value("${swagger.user:swagger}")
    private String swaggerUser;

    @Value("${swagger.password:change-me}")
    private String swaggerPassword;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter,
            RateLimitingFilter rateLimitingFilter
    ) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.rateLimitingFilter = rateLimitingFilter;
    }

    /*
     * Seguridad exclusiva para Swagger.
     * En desarrollo permite acceso libre.
     * En producción exige Basic Auth.
     */
    @Bean
    @Order(1)
    public SecurityFilterChain swaggerSecurityFilterChain(
            HttpSecurity http,
            PasswordEncoder passwordEncoder
    ) throws Exception {

        boolean isProd = "prod".equalsIgnoreCase(activeProfile);

        http
                .securityMatcher(
                        "/swagger-ui.html",
                        "/swagger-ui/**",
                        "/v3/api-docs/**"
                )
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                );

        if (isProd) {
            UserDetails swaggerAccount = User
                    .withUsername(swaggerUser)
                    .password(passwordEncoder.encode(swaggerPassword))
                    .roles("SWAGGER")
                    .build();

            InMemoryUserDetailsManager swaggerUsers =
                    new InMemoryUserDetailsManager(swaggerAccount);

            DaoAuthenticationProvider swaggerProvider =
                    new DaoAuthenticationProvider();

            swaggerProvider.setUserDetailsService(swaggerUsers);
            swaggerProvider.setPasswordEncoder(passwordEncoder);

            AuthenticationEntryPoint swaggerEntryPoint =
                    (request, response, exception) -> {
                        response.setStatus(
                                HttpServletResponse.SC_UNAUTHORIZED
                        );
                        response.setHeader(
                                HttpHeaders.WWW_AUTHENTICATE,
                                "Basic realm=\"Swagger Docs\""
                        );
                    };

            http
                    .authenticationProvider(swaggerProvider)
                    .authorizeHttpRequests(authorize -> authorize
                            .anyRequest().authenticated()
                    )
                    .exceptionHandling(exceptions -> exceptions
                            .authenticationEntryPoint(swaggerEntryPoint)
                    )
                    .httpBasic(httpBasic -> httpBasic
                            .authenticationEntryPoint(swaggerEntryPoint)
                    );

        } else {
            http.authorizeHttpRequests(authorize -> authorize
                    .anyRequest().permitAll()
            );
        }

        return http.build();
    }

    /*
     * Seguridad principal de la API mediante JWT.
     */
    @Bean
    @Order(2)
    public SecurityFilterChain apiSecurityFilterChain(
            HttpSecurity http
    ) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(
                                "/api/auth/**",
                                "/actuator/health",
                                "/api/actuator/health",
                                "/error"
                        ).permitAll()
                        
                        .requestMatchers(HttpMethod.GET, "/api/events", "/api/events/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/categories", "/api/categories/**").permitAll()
                        
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                ) 
                .addFilterBefore(
                        rateLimitingFilter,
                        UsernamePasswordAuthenticationFilter.class
                )
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        if (allowedOrigins != null && !allowedOrigins.isBlank()) {
            configuration.setAllowedOrigins(
                    Arrays.asList(allowedOrigins.split(","))
            );
        } else {
            configuration.setAllowedOrigins(List.of("*"));
        }

        configuration.setAllowedMethods(
                Arrays.asList(
                        "GET",
                        "POST",
                        "PUT",
                        "PATCH",
                        "DELETE",
                        "OPTIONS"
                )
        );

        configuration.setAllowedHeaders(
                Arrays.asList(
                        "Authorization",
                        "Content-Type",
                        "Cache-Control"
                )
        );

        configuration.setExposedHeaders(
                List.of("Content-Disposition")
        );

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}