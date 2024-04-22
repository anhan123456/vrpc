package com.expectlost.exceptions;

public class LoadBalancerException extends RuntimeException {
    public LoadBalancerException() {
    }

    public LoadBalancerException(String message) {
        super(message);
    }
}
