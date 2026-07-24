package ec.edu.ups.icc.events.core.exceptions;

import ec.edu.ups.icc.events.core.dtos.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationException(
            MethodArgumentNotValidException exception
    ) {
        Map<String, String> errors = new LinkedHashMap<>();

        exception.getBindingResult()
                .getFieldErrors()
                .forEach(error -> errors.putIfAbsent(
                        error.getField(),
                        error.getDefaultMessage() != null
                                ? error.getDefaultMessage()
                                : "Valor inválido"
                ));

        ApiResponse<Map<String, String>> response = new ApiResponse<>(
                false,
                "La solicitud contiene campos inválidos",
                errors,
                LocalDateTime.now()
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleResourceNotFound(
            ResourceNotFoundException exception
    ) {
        return buildErrorResponse(
                HttpStatus.NOT_FOUND,
                exception.getMessage()
        );
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadRequest(
            BadRequestException exception
    ) {
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                exception.getMessage()
        );
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnauthorized(
            UnauthorizedException exception
    ) {
        return buildErrorResponse(
                HttpStatus.UNAUTHORIZED,
                exception.getMessage()
        );
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ApiResponse<Void>> handleForbidden(
            ForbiddenException exception
    ) {
        return buildErrorResponse(
                HttpStatus.FORBIDDEN,
                exception.getMessage()
        );
    }

    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessRule(
            BusinessRuleException exception
    ) {
        return buildErrorResponse(
                HttpStatus.CONFLICT,
                exception.getMessage()
        );
    }

    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ApiResponse<Void>> handleRateLimitExceeded(
            RateLimitExceededException exception
    ) {
        return buildErrorResponse(
                HttpStatus.TOO_MANY_REQUESTS,
                exception.getMessage()
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpectedException(
            Exception exception
    ) {
        return buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocurrió un error interno inesperado"
        );
    }

    private ResponseEntity<ApiResponse<Void>> buildErrorResponse(
            HttpStatus status,
            String message
    ) {
        return ResponseEntity
                .status(status)
                .body(ApiResponse.error(message));
    }
}