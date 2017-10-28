package com.example.hugolucas.cca.apiObjects;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Contains whether or not a location is currently open as well as
 * Created by hugolucas on 10/27/17.
 */

public class OpeningHours {

    @SerializedName("open_now") @Expose
    private Boolean openNow;

    public Boolean getOpenNow() {
        return openNow;
    }
}
