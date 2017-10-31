package com.example.hugolucas.cca.apiObjects;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Map;

/**
 * Created by hugolucas on 10/27/17.
 */

public class FixerResult {

    @SerializedName("base") @Expose
    private String baseCurrency;

    @SerializedName("date") @Expose
    private String date;

    @SerializedName("rates") @Expose
    private Map<String, Float> rates;

    public String getBaseCurrency() {
        return baseCurrency;
    }

    public String getDate() {
        return date;
    }

    public Map<String, Float> getRates(){
        return rates;
    }
}
