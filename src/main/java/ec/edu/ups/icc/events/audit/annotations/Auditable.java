package ec.edu.ups.icc.events.audit.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Auditable {

    String action();

    String failureAction() default "";

    String resourceName() default "";

    int resourceIdArg() default -1;

    boolean captureResultId() default true;
}