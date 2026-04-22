package com.smartcampus.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

/**
 * Root Discovery Endpoint for the API.
 */

@Path("/")
public class DiscoveryResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getApiMetadata() {
        Map<String, Object> response = new HashMap<>();
        
        // API Info
        Map<String, Object> info = new HashMap<>();
        info.put("version", "1.0.0");
        info.put("name", "Smart Campus API");
        info.put("description", "RESTful API for managing campus rooms and sensors");
        
        // Contact
        Map<String, String> contact = new HashMap<>();
        contact.put("email", "chalini.20241143@iit.ac.lk");
        
        // Endpoints
        Map<String, String> endpoints = new HashMap<>();
        endpoints.put("rooms", "/api/v1/rooms");
        endpoints.put("sensors", "/api/v1/sensors");
        
        response.put("info", info);
        response.put("contact", contact);
        response.put("endpoints", endpoints);

        return Response.ok(response).build();
    }
}
