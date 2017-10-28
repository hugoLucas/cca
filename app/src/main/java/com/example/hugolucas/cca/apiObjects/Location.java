package com.example.hugolucas.cca.apiObjects;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by hugolucas on 10/27/17.
 */

public class Location{
    @SerializedName("lat") @Expose
    private Double lat;

    @SerializedName("lng") @Expose
    private Double lng;

    public Double getLat() {
        return lat;
    }

    public Double getLng() {
        return lng;
    }
}
