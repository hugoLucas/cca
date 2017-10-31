package com.example.hugolucas.cca.apiObjects;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Contains the longitude and latitude of a location.
 *
 * Created by hugolucas on 10/27/17.
 */

public class Geometry {

    @SerializedName("location") @Expose
    private Location location;

    public Location getLocation() {
        return location;
    }
}
