package com.example.hugolucas.cca;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.NumberPicker;
import android.widget.Switch;

import com.example.hugolucas.cca.constants.Settings;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;

/**
 * Handles the logic behind saving user settings.
 *
 * Created by hugolucas on 11/22/17.
 */

public class SettingsActivity extends AppCompatActivity{

    public static final String TAG = "CCA.SettingsActivity";

    @BindView(R.id.settings_switch_db_comparison)
    Switch mDBSwitch;

    @BindView(R.id.settings_switch_keep_image)
    Switch mImgSwitch;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.settings_view_history_interval)
    public void setHistoryInterval(){
        Log.v(TAG, "History interval View selected");

        final String [] intervalOptions = new String[]{ExchangeFragment.WEEK,
                ExchangeFragment.MONTH, ExchangeFragment.YEAR, ExchangeFragment.DECADE};
        final NumberPicker intervalPicker = new NumberPicker(this);
        buildPicker(intervalPicker, intervalOptions);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(intervalPicker);
        builder.setTitle("Select a time interval");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                SharedPreferences settings = getSharedPreferences(Settings.CCA, 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putString(Settings.INT, intervalOptions[intervalPicker.getValue()]);
                editor.apply();
            }
        });

        builder.create().show();
    }

    @OnClick(R.id.settings_view_home_currency)
    public void setHomeCurrency(){
        Log.v(TAG, "Home currency View selected");

        final String [] currencyOptions = getResources().getStringArray(R.array.dialog_currencies);
        final NumberPicker currencyPicker = new NumberPicker(this);
        buildPicker(currencyPicker, currencyOptions);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(currencyPicker);
        builder.setTitle("Select a new target currency");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                SharedPreferences settings = getSharedPreferences(Settings.CCA, 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putString(Settings.CUR, currencyOptions[currencyPicker.getValue()]);
                editor.apply();
            }
        });

        builder.create().show();
    }

    @OnClick(R.id.settings_view_search_radius)
    public void setSearchRadius(){
        Log.v(TAG, "Search radius View selected");

        final String [] radiusOptions = new String[]{Integer.toString(MapFragment.MIN),
                Integer.toString(MapFragment.MED), Integer.toString(MapFragment.LRG),
                Integer.toString(MapFragment.XLRG)};
        final NumberPicker radiusPicker = new NumberPicker(this);
        buildPicker(radiusPicker, radiusOptions);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(radiusPicker);
        builder.setTitle("Select a default search radius");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                SharedPreferences settings = getSharedPreferences(Settings.CCA, 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putString(Settings.RAD, radiusOptions[radiusPicker.getValue()]);
                editor.apply();
            }
        });

        builder.create().show();
    }

    @OnCheckedChanged(R.id.settings_switch_keep_image)
    public void keepImage(CompoundButton button, boolean checked){
        Log.v(TAG, "Keep image Switch selected");

        SharedPreferences settings = getSharedPreferences(Settings.CCA, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(Settings.IMG, checked);
        editor.apply();
    }

    @OnCheckedChanged(R.id.settings_switch_db_comparison)
    public void setDatabases(CompoundButton button, boolean checked){
        Log.v(TAG, "DB Switch selected");

        SharedPreferences settings = getSharedPreferences(Settings.CCA, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(Settings.DB, checked);
        editor.apply();
    }

    public void buildPicker(NumberPicker picker, String [] array){
        picker.setMinValue(0);
        picker.setValue(0);
        picker.setMaxValue(array.length - 1);
        picker.setDisplayedValues(array);
        picker.setEnabled(true);
        picker.setBackgroundColor(getResources().getColor(R.color.colorAccent));
    }
}
