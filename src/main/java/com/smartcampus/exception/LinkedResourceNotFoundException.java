package com.smartcampus.exception;

/**
 * Custom exception thrown when a dependency resource is missing.
 * Handled by LinkedResourceNotFoundExceptionMapper to return a 422 Unprocessable Entity.
 */
public class LinkedResourceNotFoundException extends RuntimeException {

    public LinkedResourceNotFoundException(String message) {
        super(message);
    }
}
