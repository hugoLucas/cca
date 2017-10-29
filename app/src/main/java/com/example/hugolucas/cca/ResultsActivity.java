package com.example.hugolucas.cca;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

/**
 * Presents the results of the classification to the User. Allows the user to access MapFragment
 * and ExchangeFragment.
 *
 * Created by hugo on 10/25/17.
 */

public class ResultsActivity extends AppCompatActivity{

    private static final String CODE = "results_activity_code";
    private static final String VAL = "results_activity_value";

    private static String mSourceCurrencyCode;
    private static String mSourceCurrencyValue;

    private static String mTargetCurrency;

    public Intent buildIntent(Context context, String sourceCurrency, String sourceCurrencyValue){
        Intent intent = new Intent(context, ResultsActivity.class);
        intent.putExtra(CODE, sourceCurrency);
        intent.putExtra(VAL, sourceCurrencyValue);

        return intent;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState,
                         @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
    }
}
