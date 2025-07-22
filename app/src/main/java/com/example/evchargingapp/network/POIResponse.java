package com.example.evchargingapp.network;

import com.google.gson.annotations.SerializedName;

public class POIResponse {
    @SerializedName("ID")
    private int id;
    @SerializedName("AddressInfo")
    private AddressInfo addressInfo;

    public int getId() { return id; }
    public AddressInfo getAddressInfo() { return addressInfo; }

    public static class AddressInfo {
        @SerializedName("Title")
        private String title;
        @SerializedName("Latitude")
        private double latitude;
        @SerializedName("Longitude")
        private double longitude;

        public String getTitle() { return title; }
        public double getLatitude() { return latitude; }
        public double getLongitude() { return longitude; }
    }
}
