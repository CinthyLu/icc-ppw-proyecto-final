package ec.edu.ups.icc.events.core.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final Environment environment;

    @Value("${swagger.user:}")
    private String swaggerUser;

    @Value("${swagger.password:}")
    private String swaggerPassword;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter,
            Environment environment
    ) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.environment = environment;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {
        boolean isProd = Arrays.asList(environment.getActiveProfiles()).contains("prod");

        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(
                                "/api/auth/**",
                                "/actuator/health",
                                "/api/actuator/health",
                                "/api/registrations/**"
                        ).permitAll()
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**")
                        .access((authentication, context) -> {
                            if (!isProd) {
                                return new org.springframework.security.authorization.AuthorizationDecision(true);
                            }
                            return new org.springframework.security.authorization.AuthorizationDecision(false);
                        })
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        if (isProd) {
            http.httpBasic(httpBasic -> httpBasic.realmName("Swagger Docs"));
        }

        return http.build();
    }

    @Bean
    public UserDetailsService swaggerUserDetailsService(PasswordEncoder passwordEncoder) {
        return new InMemoryUserDetailsManager(
                User.withUsername(swaggerUser)
                        .password(passwordEncoder.encode(swaggerPassword))
                        .roles("SWAGGER")
                        .build()
        );
    }
}