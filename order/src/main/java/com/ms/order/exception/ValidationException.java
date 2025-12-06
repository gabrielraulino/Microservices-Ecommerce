package com.ms.order.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when input data is invalid.
 * Returns HTTP 400 Bad Request.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ValidationException extends RuntimeException {

    public ValidationException(String field, String value, String reason) {
        super(String.format("Invalid %s '%s': %s", field, value, reason));
    }
}
