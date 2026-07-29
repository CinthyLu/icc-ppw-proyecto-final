package ec.edu.ups.icc.events.security.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;

@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private final StringRedisTemplate redisTemplate;
    private final ec.edu.ups.icc.events.security.config.RateLimitingProperties props;

    public RateLimitingFilter(StringRedisTemplate redisTemplate, ec.edu.ups.icc.events.security.config.RateLimitingProperties props) {
        this.redisTemplate = redisTemplate;
        this.props = props;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        // Normalizar path quitando el context-path si está presente
        String contextPath = request.getContextPath();
        if (contextPath != null && !contextPath.isEmpty() && path.startsWith(contextPath)) {
            path = path.substring(contextPath.length());
        }

        String ip = resolveIpAddress(request);
        String key;
        long limit;
        Duration duration;

        if (path.equals("/api/auth/login") || path.equals("/auth/login")) {
            String email = request.getParameter("username");
            key = "rate:login:" + ip + ":" + (email != null ? email.trim().toLowerCase() : "");
            limit = props.getLoginLimit();
            duration = Duration.ofSeconds(props.getLoginWindowSeconds());
        } else if (path.equals("/api/auth/register") || path.equals("/auth/register")) {
            key = "rate:register:" + ip;
            limit = props.getRegisterLimit();
            duration = Duration.ofSeconds(props.getRegisterWindowSeconds());
        } else if (path.startsWith("/api/reports/") || path.startsWith("/reports/")) {
            String user = getAuthenticatedUser();
            key = "rate:reports:" + user;
            limit = props.getGeneralAuthLimit();
            duration = Duration.ofSeconds(props.getGeneralWindowSeconds());
        } else if (isPublicEndpoint(path)) {
            key = "rate:public:" + ip;
            limit = props.getGeneralAnonymousLimit();
            duration = Duration.ofSeconds(props.getGeneralWindowSeconds());
        } else {
            String user = getAuthenticatedUser();
            key = "rate:auth:" + user;
            limit = props.getGeneralAuthLimit();
            duration = Duration.ofSeconds(props.getGeneralWindowSeconds());
        }

        Long currentRequests = redisTemplate.opsForValue().increment(key);
        if (currentRequests != null && currentRequests == 1) {
            redisTemplate.expire(key, duration);
        }

        if (currentRequests != null && currentRequests > limit) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setHeader("Retry-After", String.valueOf(duration.toSeconds()));
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"success\":false,\"status\":429,\"code\":\"RATE_LIMIT_EXCEEDED\",\"message\":\"Demasiadas solicitudes. Reintente en unos momentos.\",\"path\":\"" + request.getRequestURI() + "\",\"timestamp\":\"" + java.time.LocalDateTime.now() + "\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String resolveIpAddress(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal()))
                ? auth.getName()
                : "anonymous";
    }

    private boolean isPublicEndpoint(String path) {
        return path.equals("/api/auth/login") || path.equals("/api/auth/register") || path.equals("/api/auth/refresh")
                || path.startsWith("/swagger-ui") || path.startsWith("/v3/api-docs") || path.startsWith("/api/swagger-ui")
                || path.startsWith("/api/v3/api-docs") || path.equals("/actuator/health") || path.equals("/api/actuator/health");
    }
}
