package com.smartcampus.exception;

/**
 * Custom exception thrown when attempting to delete a Room that still has Sensors assigned.
 * Handled by RoomNotEmptyExceptionMapper to return a 409 Conflict response.
 */
public class RoomNotEmptyException extends RuntimeException {
    
    public RoomNotEmptyException(String message) {
        super(message);
    }
}
