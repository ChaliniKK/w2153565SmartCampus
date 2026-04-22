package com.smartcampus.mapper;

import com.smartcampus.exception.LinkedResourceNotFoundException;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.HashMap;
import java.util.Map;

@Provider
public class LinkedResourceNotFoundExceptionMapper implements ExceptionMapper<LinkedResourceNotFoundException> {

    @Override
    public Response toResponse(LinkedResourceNotFoundException exception) {
        Map<String, Object> errorBody = new HashMap<>();
        errorBody.put("status", 422);
        errorBody.put("error", "Unprocessable Entity");
        errorBody.put("message", exception.getMessage());

        return Response.status(422)
                .entity(errorBody)
                .build();
    }
}
