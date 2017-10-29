package com.example.hugolucas.cca;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.hugolucas.cca.apiObjects.FixerResult;
import com.example.hugolucas.cca.apis.FixerApi;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by hugolucas on 10/27/17.
 */

public class ExchangeFragment extends Fragment {

    private static String TAG = "exchange_fragment";

    private static final String DECADE = "DECADE";
    private static final String YEAR = "YEAR";
    private static final String MONTH = "MONTH";
    private static final String WEEK = "WEEK";

    private static int mSourceValue = 10;

    private static String mCurrentInterval = DECADE;
    private static String mSourceCurrency = "USD";
    private static String mTargetCurrency = "GBP";

    @BindView(R.id.change_target_currency) FloatingActionButton mCurrencyChangeButton;
    @BindView(R.id.toggle_time_interval) FloatingActionButton mTimeToggleButton;
    @BindView(R.id.exchange_fragment_loading_icon) ProgressBar mProgressBar;
    @BindView(R.id.exchange_fragment_chart_title) TextView mChartTitle;
    @BindView(R.id.exchange_line_chart) LineChart mLineChart;

    private DataTable mDataTable;
    private XAxisFormatter mXFormatter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_exchange, container, false);
        ButterKnife.bind(this, view);

        mLineChart.setBackgroundColor(getResources().getColor(R.color.graph_background_white));
        mLineChart.getAxisLeft().setEnabled(false);

        XAxis xAxis = mLineChart.getXAxis();
        xAxis.setGranularity(1f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        mXFormatter = new XAxisFormatter();
        xAxis.setValueFormatter(mXFormatter);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        gatherData();
    }

    /**
     * Creates a list of dates needed to explain a certain time interval and calls the Fixer API
     * to get exchange rate information for those dates. Let the query method know which call is
     * the last call in the time interval in order to trigger the update of the graph view.
     */
    public void gatherData(){
        toggleProgressBarVisibility();
        toggleGraphComponentVisibility();

        mLineChart.setEnabled(false);

        List<String> dateQueries = generateTimeLine(mCurrentInterval);
        int numberOfQueries = dateQueries.size();

        mDataTable = new DataTable(mSourceCurrency, mTargetCurrency);
        mDataTable.setCallNumber(numberOfQueries);
        mDataTable.setIndicies(dateQueries);

        for (int i = 0; i < numberOfQueries; i ++){
            final String date = dateQueries.get(i);
            if (i + 1 == numberOfQueries)
                queryDatabase(date, mDataTable.getCurrencies(), true);
            else
                queryDatabase(date, mDataTable.getCurrencies(), false);
        }
    }

    /**
     * Uses the RetroFit interface for the Fixer API to get exchange rate information
     * asynchronously. On the last call, it initiates an AsyncTask to refresh the graph.
     *
     * @param date          query parameter, the date the exchange rate data was observed
     * @param currencies    a string of format [$$$],[$$$] used to query the API
     * @param lastCall      boolean signifying if this call is the last call needed for an interval
     */
    public void queryDatabase(final String date, String currencies, final boolean lastCall){
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
                Log.v(TAG, "API call successful for: " + date);
                Map<String, Float> rates = response.body().getRates();
                mDataTable.parseMap(date, rates);
                mDataTable.incrementCalls();

                if(lastCall)
                    new PopulateGraph().execute();
            }

            @Override
            public void onFailure(Call<FixerResult> call, Throwable t) {
                Log.v(TAG, "API call failed for: " + date);
                Log.v(TAG, t.getLocalizedMessage());
                Log.v(TAG, t.toString());

                mDataTable.incrementCalls();
            }
        });
    }

    /**
     * Given a previously defined time interval, this method will create a list of String objects
     * whose value match a series of dates in those intervals. Most intervals are sampled rather
     * than fully queried due to time constraints.
     *
     * @param timeInterval      the time interval used to generate the list of String dates
     * @return                  a list of date String objects in the form of YYYY-MM-DD
     */
    public List<String> generateTimeLine(String timeInterval){
        ArrayList<String> timeline = new ArrayList<>();

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

        switch (timeInterval) {
            case WEEK: {
                // Data Points: 7
                for (int i = 0; i < 7; i ++) {
                    timeline.add(0, formatter.format(calendar.getTime()));
                    calendar.add(Calendar.DATE, -1);
                }
                break;
            }

            case MONTH: {
                // Data Points: 10
                for (int i = 0; i < 10; i ++){
                    timeline.add(0, formatter.format(calendar.getTime()));
                    calendar.add(Calendar.DATE, -3);
                }
                break;
            }

            case YEAR: {
                // Data Points: 15
                for (int i = 0; i < 15; i ++){
                    timeline.add(0, formatter.format(calendar.getTime()));
                    calendar.add(Calendar.DATE, -14);
                }
                break;
            }

            case DECADE: {
                // Data Points: 20
                for (int i = 0; i < 20; i ++){
                    timeline.add(0, formatter.format(calendar.getTime()));
                    calendar.add(Calendar.MONTH, -6);
                }
                break;
            }
        }

        return timeline;
    }

    /**
     * When the time interval button is selected, this method will increase the value of the
     * current time interval to next greatest amount. When at the largest time interval, the
     * method will reduce it to the smallest time interval.
     */
    @OnClick(R.id.toggle_time_interval)
    public void changeTimeInterval(){
        switch (mCurrentInterval) {
            case WEEK: {
                mCurrentInterval = MONTH;
                break;
            }
            case MONTH: {
                mCurrentInterval = YEAR;
                break;
            }
            case YEAR: {
                mCurrentInterval = DECADE;
                break;
            }
            case DECADE: {
                mCurrentInterval = WEEK;
                break;
            }
        }

        Log.v(TAG, "Time interval changed to: " + mCurrentInterval);
        mDataTable = new DataTable(mSourceCurrency, mTargetCurrency);
        gatherData();
    }

    @OnClick(R.id.change_target_currency)
    public void changeTargetCurrency(){

        final String [] currencyOptions = getResources().getStringArray(R.array.dialog_currencies);
        final NumberPicker mCurrencyPicker = new NumberPicker(getContext());
        mCurrencyPicker.setMinValue(0);
        mCurrencyPicker.setValue(0);
        mCurrencyPicker.setMaxValue(currencyOptions.length - 1);
        mCurrencyPicker.setDisplayedValues(currencyOptions);
        mCurrencyPicker.setEnabled(true);
        mCurrencyPicker.setBackgroundColor(getResources().getColor(R.color.colorAccent));

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(mCurrencyPicker);
        builder.setTitle("Select a New Target Currency");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mTargetCurrency = currencyOptions[mCurrencyPicker.getValue()];
                gatherData();
            }
        });

        Dialog d = builder.create();
        builder.create().show();
    }

    /**
     * Switches the visibility status of all components related to the graph view.
     */
    private void toggleGraphComponentVisibility(){
        toggleVisibility(mCurrencyChangeButton);
        toggleVisibility(mTimeToggleButton);
        toggleVisibility(mChartTitle);
        toggleVisibility(mLineChart);
    }

    /**
     * Switches the visibility of the progress bar.
     */
    private void toggleProgressBarVisibility(){
        toggleVisibility(mProgressBar);
    }

    /**
     * Switches the visibility of any View object passed as an argument.
     *
     * @param object    a View object
     */
    private void toggleVisibility(View object){
        if (object.getVisibility() == View.VISIBLE)
            object.setVisibility(View.INVISIBLE);
        else
            object.setVisibility(View.VISIBLE);
    }

    /**
     * Class used to organize the results of the Fixer API into a format readable by the Graph
     * library used for this fragment.
     */
    private class DataTable{

        private int mNumberOfCalls;
        private int mCurrentCalls = 0;

        private String mSourceCurrency;
        private String mTargetCurrency;

        private Map<String, Float> mSourceMap;
        private Map<String, Float> mTargetMap;

        private List<String> mIndiciesGenerator;

        private Map<Integer, String> mLabelMap;

        private DataTable(String source, String target){
            mSourceCurrency = source;
            mTargetCurrency = target;

            mSourceMap = new HashMap<>();
            mTargetMap = new HashMap<>();

            mLabelMap = new HashMap<>();
        }

        public synchronized void parseMap(String date, Map<String, Float> dataMap){
            for (String s: dataMap.keySet()){
                if (s.equals(mSourceCurrency))
                    mSourceMap.put(date, dataMap.get(s));
                else if (s.equals(mTargetCurrency))
                    mTargetMap.put(date, dataMap.get(s));
                else
                    throw new IllegalArgumentException("Wrong Response!");
                // assignDateAnIndex(date);
            }
        }

        public synchronized List<Entry> generateSourceEntries(){
            List<Entry> entries = new ArrayList<>();
            int index = 0;
            for (String key: mIndiciesGenerator)
                entries.add(new Entry(index++, mSourceMap.get(key)));
            return entries;
        }

        public synchronized List<Entry> generateTargetEntries(){
            List<Entry> entries = new ArrayList<>();
            int index = 0;
            for (String key: mIndiciesGenerator)
                entries.add(new Entry(index++, mTargetMap.get(key)));
            return entries;
        }

        public synchronized List<Entry> generateValueEntries(){
            List<Entry> entries = new ArrayList<>();
            int index = 0;
            for (String key: mIndiciesGenerator)
                entries.add(new Entry(index++, (float) mSourceValue * mTargetMap.get(key)));
            return entries;
        }

        public synchronized String getXAxisLabel(int index){
            try {
                return mIndiciesGenerator.get(index);
            }catch (IndexOutOfBoundsException e){
                return null;
            }
        }

        public synchronized void setCallNumber(int calls){
            mNumberOfCalls = calls;
        }

        public synchronized void setIndicies(List<String> indicies){
            mIndiciesGenerator = indicies;
        }

        public synchronized boolean allCallsComplete(){
            return mNumberOfCalls == mCurrentCalls;
        }

        public synchronized void incrementCalls() {
            mCurrentCalls += 1;
        }

        public String getCurrencies(){
            return mSourceCurrency + "," + mTargetCurrency;
        }
    }

    /**
     * AsyncTask used to populate the Graph View with the data generated by the Fixer API.
     */
    private class PopulateGraph extends AsyncTask<Void, Void, Void>{

        private LineData data;

        @Override
        protected Void doInBackground(Void... voids) {

            while(! mDataTable.allCallsComplete()) {}

            List<Entry> sourceEntries = mDataTable.generateSourceEntries();
            List<Entry> targetEntries = mDataTable.generateTargetEntries();
            List<Entry> valueEntries = mDataTable.generateValueEntries();

            LineDataSet sourceData = new LineDataSet(sourceEntries, mSourceCurrency);
            sourceData.setColor(getResources().getColor(R.color.graph_data_blue));

            LineDataSet targetData = new LineDataSet(targetEntries, mTargetCurrency);
            targetData.setColor(getResources().getColor(R.color.graph_data_red));

            LineDataSet valueData = new LineDataSet(valueEntries,
                    getString(R.string.graph_converted_value_data_label));
            valueData.setColor(getResources().getColor(R.color.graph_data_green));

            data = new LineData();
            data.addDataSet(sourceData);
            data.addDataSet(targetData);
            data.addDataSet(valueData);

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            mLineChart.setEnabled(true);

            if (mLineChart.getLineData() != null)
                mLineChart.clearValues();

            mLineChart.setData(data);
            mChartTitle.setText(mCurrentInterval);

            mLineChart.invalidate();
            toggleGraphComponentVisibility();
            toggleProgressBarVisibility();
        }
    }


    private class XAxisFormatter implements IAxisValueFormatter{

        /**
         * Called when a value from an axis is to be formatted
         * before being drawn. For performance reasons, avoid excessive calculations
         * and memory allocations inside this method.
         *
         * @param value the value to be formatted
         * @param axis  the axis the value belongs to
         * @return      string label for float
         */
        @Override
        public String getFormattedValue(float value, AxisBase axis) {
            return mDataTable.getXAxisLabel((int) value);
        }
    }
}
