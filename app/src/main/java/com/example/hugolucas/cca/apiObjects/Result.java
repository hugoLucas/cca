package com.example.hugolucas.cca.apiObjects;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Encapsulates a single result, or location, returned in the JSON object received from the
 * Google Maps API.
 *
 * Created by hugolucas on 10/27/17.
 */

public class Result {
    @SerializedName("geometry") @Expose
    private Geometry geometry;

    @SerializedName("icon") @Expose
    private String icon;

    @SerializedName("name") @Expose
    private String name;

    @SerializedName("opening_hours") @Expose
    private OpeningHours openingHours;

    @SerializedName("rating") @Expose
    private Double rating;

    @SerializedName("vicinity") @Expose
    private String vicinity;

    public Geometry getGeometry() {
        return geometry;
    }

    public String getIcon() {
        return icon;
    }

    public String getName() {
        return name;
    }

    public OpeningHours getOpeningHours() {
        return openingHours;
    }

    public Double getRating() {
        return rating;
    }

    public String getVicinity() {
        return vicinity;
    }
}
