package com.smartcampus.store;

import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory singleton DataStore to act as our "database".
 * Uses ConcurrentHashMap to ensure thread safety across concurrent JAX-RS requests.
 */
public class DataStore {

    private static final DataStore instance = new DataStore();

    // Data maps
    private final Map<String, Room> rooms = new ConcurrentHashMap<>();
    private final Map<String, Sensor> sensors = new ConcurrentHashMap<>();
    private final Map<String, List<SensorReading>> sensorReadings = new ConcurrentHashMap<>();

    private DataStore() {
        // Optional: Pre-populate with some initial dummy data for easy testing
        Room r1 = new Room("Library Quiet Study", "Library", 3);
        r1.setId("LIB-301"); // Force ID so sensors dummy data maps correctly
        rooms.put(r1.getId(), r1);
        
        Sensor s1 = new Sensor("TEMP-001", "Temperature", "ACTIVE", 21.5, "LIB-301");
        sensors.put(s1.getId(), s1);
        r1.getSensorIds().add(s1.getId());
        
        sensorReadings.put(s1.getId(), new ArrayList<>());
        sensorReadings.get(s1.getId()).add(new SensorReading("rdg-1", System.currentTimeMillis(), 21.5));
    }

    public static DataStore getInstance() {
        return instance;
    }

    public Map<String, Room> getRooms() {
        return rooms;
    }

    public Map<String, Sensor> getSensors() {
        return sensors;
    }

    public Map<String, List<SensorReading>> getSensorReadings() {
        return sensorReadings;
    }
}
