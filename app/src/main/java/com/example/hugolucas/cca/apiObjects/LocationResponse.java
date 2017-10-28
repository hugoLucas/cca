package com.example.hugolucas.cca.apiObjects;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Encapsulates the response main JSON object sent back by the Google Maps API.
 *
 * Created by hugolucas on 10/27/17.
 */

public class LocationResponse {

    @SerializedName("html_attribution") @Expose
    private List<Object> attributions;

    @SerializedName("next_page_token") @Expose
    private String token;

    @SerializedName("results") @Expose
    private List<Result> results;

    @SerializedName("status") @Expose
    private String status;

    public List<Object> getAttributions() {
        return attributions;
    }

    public String getToken() {
        return token;
    }

    public List<Result> getResults() {
        return results;
    }

    public String getStatus() {
        return status;
    }
}
