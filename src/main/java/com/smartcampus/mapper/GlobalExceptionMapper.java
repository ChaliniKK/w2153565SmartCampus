package com.smartcampus.mapper;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The Global Safety Net.
 * Intercepts any unhandled RuntimeException/Throwable to prevent 
 * raw Java stack traces from leaking to the client.
 */
@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger LOGGER = Logger.getLogger(GlobalExceptionMapper.class.getName());

    @Override
    public Response toResponse(Throwable exception) {
        // Log the actual exception trace on the server side for debugging
        LOGGER.log(Level.SEVERE, "Unhandled exception occurred", exception);

        // Return a safe, sanitized generic error to the client
        Map<String, Object> errorBody = new HashMap<>();
        errorBody.put("status", 500);
        errorBody.put("error", "Internal Server Error");
        errorBody.put("message", "An unexpected error occurred. Please try again later or contact support.");

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(errorBody)
                .build();
    }
}
