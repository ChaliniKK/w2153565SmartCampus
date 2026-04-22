package com.smartcampus.resource;

import com.smartcampus.exception.LinkedResourceNotFoundException;
import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;
import com.smartcampus.store.DataStore;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorResource {

    private final DataStore store = DataStore.getInstance();

    @GET
    public Response getSensors(@QueryParam("type") String type) {
        Collection<Sensor> sensors = store.getSensors().values();
        
        if (type != null && !type.trim().isEmpty()) {
            List<Sensor> filteredSensors = sensors.stream()
                    .filter(s -> type.equalsIgnoreCase(s.getType()))
                    .collect(Collectors.toList());
            return Response.ok(filteredSensors).build();
        }
        
        return Response.ok(sensors).build();
    }

    @POST
    public Response registerSensor(Sensor sensor, @Context UriInfo uriInfo) {
        // Validate Room exists
        String roomId = sensor.getRoomId();
        Room room = store.getRooms().get(roomId);
        if (room == null) {
            throw new LinkedResourceNotFoundException("Room with ID " + roomId + " does not exist.");
        }

        if (sensor.getId() == null || sensor.getId().trim().isEmpty()) {
            sensor.setId("SENS-" + UUID.randomUUID().toString().substring(0, 8));
        }
        
        if (sensor.getStatus() == null || sensor.getStatus().trim().isEmpty()) {
            sensor.setStatus("ACTIVE");
        }

        store.getSensors().put(sensor.getId(), sensor);
        room.getSensorIds().add(sensor.getId());
        
        // Initialize an empty reading list for this sensor
        store.getSensorReadings().put(sensor.getId(), new ArrayList<>());

        URI location = uriInfo.getAbsolutePathBuilder().path(sensor.getId()).build();
        return Response.created(location).entity(sensor).build();
    }
    
    /**
     * Sub-resource locator for SensorReadings.
     * Part 4 functionality.
     */
    @Path("/{sensorId}/readings")
    public SensorReadingResource getSensorReadingResource(@PathParam("sensorId") String sensorId) {
        return new SensorReadingResource(sensorId);
    }
}
