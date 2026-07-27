package ec.edu.ups.icc.events.audit.aspects;

import ec.edu.ups.icc.events.audit.annotations.Auditable;
import ec.edu.ups.icc.events.audit.services.AuditLogService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.UUID;

@Aspect
@Component
public class AuditAspect {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(AuditAspect.class);

    private final AuditLogService auditLogService;

    public AuditAspect(
            AuditLogService auditLogService
    ) {
        this.auditLogService = auditLogService;
    }

    @Around("@annotation(auditable)")
    public Object auditMethod(
            ProceedingJoinPoint joinPoint,
            Auditable auditable
    ) throws Throwable {

        try {
            Object result = joinPoint.proceed();

            String resourceId = resolveResourceId(
                    joinPoint,
                    result,
                    auditable
            );

            safeRecord(
                    auditable.action(),
                    auditable.resourceName(),
                    resourceId,
                    buildSuccessDetails(joinPoint)
            );

            return result;
        } catch (Throwable exception) {
            if (!auditable.failureAction().isBlank()) {
                String resourceId = resolveResourceId(
                        joinPoint,
                        null,
                        auditable
                );

                safeRecord(
                        auditable.failureAction(),
                        auditable.resourceName(),
                        resourceId,
                        buildFailureDetails(
                                joinPoint,
                                exception
                        )
                );
            }

            throw exception;
        }
    }

    private void safeRecord(
            String action,
            String resourceName,
            String resourceId,
            String details
    ) {
        try {
            auditLogService.record(
                    action,
                    resourceName,
                    resourceId,
                    details
            );
        } catch (RuntimeException exception) {
            LOGGER.warn(
                    "No se pudo registrar la auditoría para la acción {}",
                    action,
                    exception
            );
        }
    }

    private String resolveResourceId(
            ProceedingJoinPoint joinPoint,
            Object result,
            Auditable auditable
    ) {
        int argumentIndex = auditable.resourceIdArg();

        if (argumentIndex >= 0) {
            Object[] arguments = joinPoint.getArgs();

            if (argumentIndex < arguments.length) {
                return extractId(arguments[argumentIndex]);
            }
        }

        if (auditable.captureResultId()) {
            return extractId(result);
        }

        return null;
    }

    private String extractId(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof ResponseEntity<?> response) {
            return extractId(response.getBody());
        }

        if (value instanceof Map<?, ?> map) {
            return extractId(map.get("id"));
        }

        if (value instanceof Number
                || value instanceof CharSequence
                || value instanceof UUID) {
            return value.toString();
        }

        String idFromGetter = invokeIdMethod(
                value,
                "getId"
        );

        if (idFromGetter != null) {
            return idFromGetter;
        }

        return invokeIdMethod(value, "id");
    }

    private String invokeIdMethod(
            Object value,
            String methodName
    ) {
        try {
            Method method = value
                    .getClass()
                    .getMethod(methodName);

            Object id = method.invoke(value);

            return id == null
                    ? null
                    : id.toString();
        } catch (ReflectiveOperationException exception) {
            return null;
        }
    }

    private String buildSuccessDetails(
            ProceedingJoinPoint joinPoint
    ) {
        return "Operación completada: "
                + joinPoint.getSignature().toShortString();
    }

    private String buildFailureDetails(
            ProceedingJoinPoint joinPoint,
            Throwable exception
    ) {
        String message = exception.getMessage();

        if (message == null || message.isBlank()) {
            message = "Sin mensaje adicional";
        }

        return "Operación fallida: "
                + joinPoint.getSignature().toShortString()
                + " | "
                + exception.getClass().getSimpleName()
                + ": "
                + message;
    }
}