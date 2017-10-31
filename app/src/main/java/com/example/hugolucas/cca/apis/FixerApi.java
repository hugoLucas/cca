package com.example.hugolucas.cca.apis;

import com.example.hugolucas.cca.apiObjects.FixerResult;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by hugolucas on 10/27/17.
 */

public interface FixerApi {

    @GET("/latest")
    Call<FixerResult> getLatestExchangeRate(
            @Query("symbols") String currencies
    );

    @GET("/{date}")
    Call<FixerResult> getExchangeRates(
            @Path("date") String date,
            @Query("symbols") String currencies
    );
}
