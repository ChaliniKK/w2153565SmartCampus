package com.smartcampus.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Room {

    private String id;       // Unique identifier
    private String name;     // Human-readable name
    private String building; // Building name
    private int floor;       // Floor number
    private List<String> sensorIds = new ArrayList<>();

    public Room() {
        this.id = UUID.randomUUID().toString();
    }

    public Room(String name, String building, int floor) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.building = building;
        this.floor = floor;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getBuilding() { return building; }
    public void setBuilding(String building) { this.building = building; }

    public int getFloor() { return floor; }
    public void setFloor(int floor) { this.floor = floor; }

    public List<String> getSensorIds() { return sensorIds; }
    public void setSensorIds(List<String> sensorIds) { this.sensorIds = sensorIds; }
    
    public void addSensorId(String sensorId) {
        this.sensorIds.add(sensorId);
    }
    
    public boolean hasSensors() {
        return !sensorIds.isEmpty();
    }
}
