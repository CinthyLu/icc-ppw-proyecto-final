package ec.edu.ups.icc.events.core.exceptions;

import org.springframework.http.HttpStatus;

public class AccountLockedException extends ApplicationException {
    public AccountLockedException(String message) {
        super(HttpStatus.LOCKED, message);
    }
}
