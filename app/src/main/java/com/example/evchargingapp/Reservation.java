package com.example.evchargingapp;

public class Reservation {
    private String id;
    private String userId;
    private String stationId;
    private long startTime; // Unix timestamp (ms)
    private long endTime;   // Unix timestamp (ms)

    public Reservation() {
        // Firestore için boş constructor
    }

    public Reservation(String id, String userId, String stationId, long startTime, long endTime) {
        this.id = id;
        this.userId = userId;
        this.stationId = stationId;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    // Getters
    public String getId() { return id; }
    public String getUserId() { return userId; }
    public String getStationId() { return stationId; }
    public long getStartTime() { return startTime; }
    public long getEndTime() { return endTime; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setUserId(String userId) { this.userId = userId; }
    public void setStationId(String stationId) { this.stationId = stationId; }
    public void setStartTime(long startTime) { this.startTime = startTime; }
    public void setEndTime(long endTime) { this.endTime = endTime; }
}
