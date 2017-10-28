package com.example.hugolucas.cca.api;

import com.example.hugolucas.cca.apiObjects.LocationResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * A Java interface for the Google Maps API using the Retrofit library.
 *
 * Created by hugolucas on 10/27/17.
 */

public interface GoogleMaps {

    @GET("api/place/nearbysearch/json")
    Call<LocationResponse> nearbyLocations(
        @Query("sensor") String sensor,
        @Query("key") String key,
        @Query("type") String type,
        @Query("location") String location,
        @Query("radius") String radius
    );
}
