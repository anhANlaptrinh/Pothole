package com.example.pothole.model;

public class Pothole {
    private String id;
    private double latitude;
    private double longitude;
    private int severity;

    public Pothole() {
    }

    // Constructor đầy đủ tham số
    public Pothole(String id, double latitude, double longitude, int severity) {
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
        this.severity = severity;
    }

    public Pothole(double latitude, double longitude, int severity) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.severity = severity;
    }
    // Getters và Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public int getSeverity() {
        return severity;
    }

    public void setSeverity(int severity) {
        this.severity = severity;
    }
}
