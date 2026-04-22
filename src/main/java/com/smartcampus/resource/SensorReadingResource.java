package com.smartcampus.resource;

import com.smartcampus.exception.SensorUnavailableException;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;
import com.smartcampus.store.DataStore;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.List;
import java.util.UUID;

/**
 * Sub-resource handling readings for a specific sensor.
 */
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorReadingResource {

    private final String sensorId;
    private final DataStore store = DataStore.getInstance();

    public SensorReadingResource(String sensorId) {
        this.sensorId = sensorId;
    }

    @GET
    public Response getReadings() {
        Sensor sensor = store.getSensors().get(sensorId);
        if (sensor == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\":\"Sensor not found\"}").build();
        }

        List<SensorReading> readings = store.getSensorReadings().get(sensorId);
        return Response.ok(readings).build();
    }

    @POST
    public Response addReading(SensorReading reading, @Context UriInfo uriInfo) {
        Sensor sensor = store.getSensors().get(sensorId);
        if (sensor == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\":\"Sensor not found\"}").build();
        }

        if ("MAINTENANCE".equalsIgnoreCase(sensor.getStatus()) || 
    "OFFLINE".equalsIgnoreCase(sensor.getStatus())) {
    throw new SensorUnavailableException("Sensor " + sensorId + " is in " + sensor.getStatus() + " status and cannot accept readings.");
}
        if (!"ACTIVE".equalsIgnoreCase(sensor.getStatus())) {
    throw new SensorUnavailableException(
        "Sensor " + sensorId + " is in " + sensor.getStatus() + 
        " status and cannot accept readings. Only ACTIVE sensors can record data."
    );
}

        if (reading.getId() == null || reading.getId().trim().isEmpty()) {
            reading.setId("RDG-" + UUID.randomUUID().toString());
        }
        
        if (reading.getTimestamp() == 0) {
            reading.setTimestamp(System.currentTimeMillis());
        }

        sensor.setCurrentValue(reading.getValue());

        store.getSensorReadings().get(sensorId).add(reading);

        URI location = uriInfo.getAbsolutePathBuilder().path(reading.getId()).build();
        return Response.created(location).entity(reading).build();
    }
}
