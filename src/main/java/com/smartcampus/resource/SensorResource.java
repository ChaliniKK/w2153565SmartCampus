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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

        // Check for duplicate sensor ID
        if (sensor.getId() != null && !sensor.getId().trim().isEmpty()) {
            if (store.getSensors().containsKey(sensor.getId())) {
                Map<String, Object> errorBody = new HashMap<>();
                errorBody.put("status", 409);
                errorBody.put("error", "Conflict");
                errorBody.put("message", "Sensor with ID " + sensor.getId() + " already exists.");
                return Response.status(Response.Status.CONFLICT)
                        .entity(errorBody)
                        .build();
            }
        } else {
            sensor.setId("SENS-" + UUID.randomUUID().toString().substring(0, 8));
        }
        
        if (sensor.getStatus() == null || sensor.getStatus().trim().isEmpty()) {
            sensor.setStatus("ACTIVE");
        }

        store.getSensors().put(sensor.getId(), sensor);
        room.getSensorIds().add(sensor.getId());
        
        // Initialize an empty reading list
        store.getSensorReadings().put(sensor.getId(), new ArrayList<>());

        URI location = uriInfo.getAbsolutePathBuilder().path(sensor.getId()).build();
        return Response.created(location).entity(sensor).build();
    }
    
    @GET
    @Path("/{sensorId}")
    public Response getSensor(@PathParam("sensorId") String sensorId) {
        Sensor sensor = store.getSensors().get(sensorId);
        if (sensor == null) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Sensor with ID " + sensorId + " not found");
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(error)
                    .build();
        }
        return Response.ok(sensor).build();
    }

    @PUT
    @Path("/{sensorId}")
    public Response updateSensor(@PathParam("sensorId") String sensorId, Sensor updatedSensor) {
        Sensor existingSensor = store.getSensors().get(sensorId);
        
        // Check if sensor exists
        if (existingSensor == null) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Sensor with ID " + sensorId + " not found");
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(error)
                    .build();
        }
        
        // Validate room exists if roomId is being changed
        String newRoomId = updatedSensor.getRoomId();
        if (newRoomId != null && !newRoomId.trim().isEmpty()) {
            Room room = store.getRooms().get(newRoomId);
            if (room == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Room with ID " + newRoomId + " does not exist");
                return Response.status(422) // Unprocessable Entity
                        .entity(error)
                        .build();
            }
            
            // If room changed, update both rooms' sensor lists
            String oldRoomId = existingSensor.getRoomId();
            if (!newRoomId.equals(oldRoomId)) {
                // Remove from old room
                Room oldRoom = store.getRooms().get(oldRoomId);
                if (oldRoom != null) {
                    oldRoom.getSensorIds().remove(sensorId);
                }
                // Add to new room
                room.getSensorIds().add(sensorId);
            }
        }
        
        // Update fields (only update non-null fields from request)
        if (updatedSensor.getType() != null) {
            existingSensor.setType(updatedSensor.getType());
        }
        if (updatedSensor.getStatus() != null) {
            existingSensor.setStatus(updatedSensor.getStatus());
        }
        if (updatedSensor.getCurrentValue() != 0) {
            existingSensor.setCurrentValue(updatedSensor.getCurrentValue());
        }
        if (updatedSensor.getRoomId() != null && !updatedSensor.getRoomId().trim().isEmpty()) {
            existingSensor.setRoomId(updatedSensor.getRoomId());
        }
        
        // Save back to store
        store.getSensors().put(sensorId, existingSensor);
        
        return Response.ok(existingSensor).build();
    }

    @DELETE
    @Path("/{sensorId}")
    public Response deleteSensor(@PathParam("sensorId") String sensorId) {
        Sensor sensor = store.getSensors().get(sensorId);
        
        if (sensor == null) {
            return Response.noContent().build();
        }
        
        // Remove from the associated room
        String roomId = sensor.getRoomId();
        if (roomId != null) {
            Room room = store.getRooms().get(roomId);
            if (room != null) {
                room.getSensorIds().remove(sensorId);
            }
        }
        
        // Remove associated readings
        store.getSensorReadings().remove(sensorId);
        
        // Remove the sensor itself
        store.getSensors().remove(sensorId);
        
        return Response.noContent().build();
    }

    /**
     * Sub-resource locator for SensorReadings.
     */
    @Path("/{sensorId}/readings")
    public SensorReadingResource getSensorReadingResource(@PathParam("sensorId") String sensorId) {
        return new SensorReadingResource(sensorId);
    }
}
