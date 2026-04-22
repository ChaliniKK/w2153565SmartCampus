package com.smartcampus.mapper;

import com.smartcampus.exception.SensorUnavailableException;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.HashMap;
import java.util.Map;

@Provider
public class SensorUnavailableExceptionMapper implements ExceptionMapper<SensorUnavailableException> {

    @Override
    public Response toResponse(SensorUnavailableException exception) {
        Map<String, Object> errorBody = new HashMap<>();
        errorBody.put("status", 403);
        errorBody.put("error", "Forbidden");
        errorBody.put("message", exception.getMessage());

        return Response.status(Response.Status.FORBIDDEN)
                .entity(errorBody)
                .build();
    }
}
