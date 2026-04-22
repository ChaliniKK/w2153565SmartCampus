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
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("api_version", "1.0");
        metadata.put("description", "Smart Campus Sensor & Room Management API");
        metadata.put("admin_contact", "admin@smartcampus.westminster.ac.uk");
        
        Map<String, String> links = new HashMap<>();
        links.put("rooms", "/api/v1/rooms");
        links.put("sensors", "/api/v1/sensors");
        
        metadata.put("_links", links);

        return Response.ok(metadata).build();
    }
}
