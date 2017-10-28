package com.example.hugolucas.cca;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.hugolucas.cca.apiObjects.FixerResult;
import com.example.hugolucas.cca.apis.FixerApi;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * A Java interface for the Fixer.io API using the Retrofit library.
 *
 * Created by hugolucas on 10/27/17.
 */

public class ExchangeFragment extends Fragment {

    private static String TAG = "exchange_fragment";

    private static final String DECADE = "DECADE";
    private static final String YEAR = "YEAR";
    private static final String MONTH = "MONTH";
    private static final String WEEK = "WEEK";

    @BindView(R.id.toggle_time_interval) FloatingActionButton mTimeToggleButton;
    @BindView(R.id.change_target_currency) FloatingActionButton mCurrencyChangeButton;

    private DataTable mDataTable;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDataTable = new DataTable("USD", "GBP");
        generateTimeLine(MONTH);
    }

    @Nullable @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_exchange, container, false);
        ButterKnife.bind(this, view);

        queryDatabase("2017-10-27", mDataTable.getCurrencies());
        return view;
    }

    public void gatherData(){

    }

    public void queryDatabase(final String date, String currencies){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.fixer.io/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        FixerApi fixer = retrofit.create(FixerApi.class);
        Call<FixerResult> result = fixer.getExchangeRates(date, currencies);
        Log.v(TAG, result.toString());
        result.enqueue(new Callback<FixerResult>() {

            @Override
            public void onResponse(Call<FixerResult> call, Response<FixerResult> response) {
                Toast.makeText(getActivity(), "Success :)", Toast.LENGTH_LONG).show();
                Map<String, Float> rates = response.body().getRates();
                mDataTable.parseMap(date, rates);
            }

            @Override
            public void onFailure(Call<FixerResult> call, Throwable t) {
                Toast.makeText(getActivity(), "Error :(", Toast.LENGTH_LONG).show();
                Log.v(TAG, t.getLocalizedMessage());
                Log.v(TAG, t.toString());
            }
        });
    }

    public List<String> generateTimeLine(String timeInterval){
        ArrayList<String> timeline = new ArrayList<>();

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

        switch (timeInterval) {
            case WEEK: {
                for (int i = 0; i < 7; i ++) {
                    timeline.add(formatter.format(calendar.getTime()));
                    calendar.add(Calendar.DATE, -1);
                }
            }

            case MONTH: {
                for (int i = 0; i < 15; i += 3){
                    timeline.add(formatter.format(calendar.getTime()));
                    calendar.add(Calendar.DATE, -2);
                }
            }

            case YEAR: {
                for (int i = 0; i < 26; i += 3){
                    timeline.add(formatter.format(calendar.getTime()));
                    calendar.add(Calendar.WEEK_OF_YEAR, -2);
                }
            }

            case DECADE: {
                for (int i = 0; i < 20; i += 3){
                    timeline.add(formatter.format(calendar.getTime()));
                    calendar.add(Calendar.MONTH, -6);
                }
            }
        }

        return timeline;
    }

    private class DataTable{

        private String mSourceCurrency;
        private String mTargetCurrency;

        private Map<String, Float> mSourceMap;
        private Map<String, Float> mTargetMap;

        private DataTable(String source, String target){
            mSourceCurrency = source;
            mTargetCurrency = target;

            mSourceMap = new HashMap<>();
            mTargetMap = new HashMap<>();
        }

        public void parseMap(String date, Map<String, Float> dataMap){
            for (String s: dataMap.keySet()){
                if (s.equals(mSourceCurrency))
                    mSourceMap.put(date, dataMap.get(s));
                else if (s.equals(mTargetCurrency))
                    mTargetMap.put(date, dataMap.get(s));
                else
                    throw new IllegalArgumentException("Wrong Response!");
            }
        }

        public String getCurrencies(){
            return mSourceCurrency + "," + mTargetCurrency;
        }
    }
}
