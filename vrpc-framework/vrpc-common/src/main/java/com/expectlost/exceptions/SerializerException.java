package com.expectlost.exceptions;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

public class SerializerException extends RuntimeException{
    public SerializerException() {
    }

    public SerializerException(String message) {
        super(message);
    }
}
