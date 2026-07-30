package ec.edu.ups.icc.events.core.exceptions;

import org.springframework.http.HttpStatus;

public class BusinessRuleException extends ApplicationException {

    public BusinessRuleException(String message) {
        super(HttpStatus.CONFLICT, message);
    }
}