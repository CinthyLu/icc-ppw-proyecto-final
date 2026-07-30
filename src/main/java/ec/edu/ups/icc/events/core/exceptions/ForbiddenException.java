package ec.edu.ups.icc.events.core.exceptions;

import org.springframework.http.HttpStatus;

public class ForbiddenException extends ApplicationException {

    public ForbiddenException(String message) {
        super(HttpStatus.FORBIDDEN, message);
    }
}