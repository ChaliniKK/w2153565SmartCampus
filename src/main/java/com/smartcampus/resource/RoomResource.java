package com.smartcampus.resource;

import com.smartcampus.exception.RoomNotEmptyException;
import com.smartcampus.model.Room;
import com.smartcampus.store.DataStore;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RoomResource {

    private final DataStore store = DataStore.getInstance();

    @GET
    public Response getAllRooms() {
        Collection<Room> rooms = store.getRooms().values();
        return Response.ok(rooms).build();
    }

    @POST
    public Response createRoom(Room room, @Context UriInfo uriInfo) {
        // Generate ID if not present
        if (room.getId() == null || room.getId().trim().isEmpty()) {
            room.setId(UUID.randomUUID().toString());
        }
        
        // Check for Duplicates
        if (store.getRooms().containsKey(room.getId())) {
            Map<String, Object> errorBody = new HashMap<>();
            errorBody.put("status", 409);
            errorBody.put("error", "Conflict");
            errorBody.put("message", "Room with ID " + room.getId() + " already exists.");
            return Response.status(Response.Status.CONFLICT)
                    .entity(errorBody)
                    .build();
        }
        
        store.getRooms().put(room.getId(), room);
        
        URI location = uriInfo.getAbsolutePathBuilder().path(room.getId()).build();
        return Response.created(location).entity(room).build();
    }

    @GET
    @Path("/{roomId}")
    public Response getRoom(@PathParam("roomId") String roomId) {
        Room room = store.getRooms().get(roomId);
        if (room == null) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Room with ID " + roomId + " not found");
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(error)
                    .build();
        }
        return Response.ok(room).build();
    }

    @PUT
    @Path("/{roomId}")
    public Response updateRoom(@PathParam("roomId") String roomId, Room updatedRoom) {
        Room existingRoom = store.getRooms().get(roomId);
        
        // Check if room exists
        if (existingRoom == null) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Room with ID " + roomId + " not found");
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(error)
                    .build();
        }
        
        // Update fields (only update non-null fields from request)
        if (updatedRoom.getName() != null && !updatedRoom.getName().trim().isEmpty()) {
            existingRoom.setName(updatedRoom.getName());
        }
        if (updatedRoom.getBuilding() != null && !updatedRoom.getBuilding().trim().isEmpty()) {
            existingRoom.setBuilding(updatedRoom.getBuilding());
        }
        if (updatedRoom.getFloor() != 0) {
            existingRoom.setFloor(updatedRoom.getFloor());
        }
        
        // Save back to store
        store.getRooms().put(roomId, existingRoom);
        
        return Response.ok(existingRoom).build();
    }

    @DELETE
    @Path("/{roomId}")
    public Response deleteRoom(@PathParam("roomId") String roomId) {
        Room room = store.getRooms().get(roomId);
        
        if (room == null) {
            return Response.noContent().build();
        }
        
        if (room.hasSensors()) {
            throw new RoomNotEmptyException("Room " + roomId + " cannot be deleted as it still has sensors assigned.");
        }
        
        store.getRooms().remove(roomId);
        return Response.noContent().build();
    }
}
