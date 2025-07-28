package com.example.evchargingapp;

public class ChargingStation {
    private String id;
    private String name;
    private double latitude;
    private double longitude;
    private boolean isAvailable;
    private double pricePerKwh;
    private String pricingType;


    public ChargingStation(String id, String name, double latitude, double longitude,
                           boolean isAvailable, double pricePerKwh, String pricingType) {
        this.id = id;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.isAvailable = isAvailable;
        this.pricePerKwh = pricePerKwh;
        this.pricingType = pricingType;
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public boolean isAvailable() { return isAvailable; }
    public double getPricePerKwh() { return pricePerKwh; }
    public String getPricingType() { return pricingType; }

    // Setters
    public void setAvailable(boolean available) { isAvailable = available; }
}
