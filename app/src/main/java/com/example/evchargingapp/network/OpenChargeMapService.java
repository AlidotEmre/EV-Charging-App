package com.example.evchargingapp.network;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface OpenChargeMapService {
    @GET("v3/poi/")
    Call<List<POIResponse>> getPOIs(
            @Query("output")       String output,
            @Query("latitude")     double lat,
            @Query("longitude")    double lon,
            @Query("distance")     int distanceKm,
            @Query("distanceunit") String distanceUnit,
            @Query("maxresults")   int maxResults,
            @Query("key")          String apiKey
    );
}
