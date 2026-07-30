package ec.edu.ups.icc.events.core.exceptions;

import org.springframework.http.HttpStatus;

public class RateLimitExceededException extends ApplicationException {

    public RateLimitExceededException(String message) {
        super(HttpStatus.TOO_MANY_REQUESTS, message);
    }
}