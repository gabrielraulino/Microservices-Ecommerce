package com.ms.product.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ValidationException extends RuntimeException {
    public ValidationException(String field, String value, String message) {
        super(String.format("Validation failed for field '%s' with value '%s': %s", field, value, message));
    }
}
