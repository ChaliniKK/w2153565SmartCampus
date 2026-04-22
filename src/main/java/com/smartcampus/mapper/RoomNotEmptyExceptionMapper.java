package com.smartcampus.mapper;

import com.smartcampus.exception.RoomNotEmptyException;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.HashMap;
import java.util.Map;

@Provider
public class RoomNotEmptyExceptionMapper implements ExceptionMapper<RoomNotEmptyException> {

    @Override
    public Response toResponse(RoomNotEmptyException exception) {
        Map<String, Object> errorBody = new HashMap<>();
        errorBody.put("status", 409);
        errorBody.put("error", "Conflict");
        errorBody.put("message", exception.getMessage());

        return Response.status(Response.Status.CONFLICT)
                .entity(errorBody)
                .build();
    }
}
