package com.smartcampus.exception;

/**
 * Custom exception thrown when trying to add a reading to a Sensor in MAINTENANCE status.
 * Handled by SensorUnavailableExceptionMapper to return a 403 Forbidden.
 */
public class SensorUnavailableException extends RuntimeException {

    public SensorUnavailableException(String message) {
        super(message);
    }
}
