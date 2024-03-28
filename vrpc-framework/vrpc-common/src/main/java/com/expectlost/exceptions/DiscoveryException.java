package com.expectlost.exceptions;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

public class DiscoveryException extends RuntimeException {
    public DiscoveryException() {
    }

    public DiscoveryException(String message) {
        super(message);
    }

    public DiscoveryException(Throwable cause) {
        super(cause);
    }
}
