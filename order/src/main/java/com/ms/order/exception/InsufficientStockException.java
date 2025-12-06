package com.ms.order.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when there is not enough stock for an operation.
 * Returns HTTP 409 Conflict.
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class InsufficientStockException extends RuntimeException {
    
    public InsufficientStockException(String message) {
        super(message);
    }
    
    public InsufficientStockException(String productName, int requested, int available) {
        super(String.format("Insufficient stock for %s. Requested: %d, Available: %d", 
                          productName, requested, available));
    }
    
    public InsufficientStockException(Long productId, int requested, int available) {
        super(String.format("Insufficient stock for product %d. Requested: %d, Available: %d", 
                          productId, requested, available));
    }
}
