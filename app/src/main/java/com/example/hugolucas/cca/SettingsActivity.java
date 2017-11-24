package com.example.hugolucas.cca;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.Switch;

import com.example.hugolucas.cca.constants.Settings;

import java.io.IOException;
import java.io.InputStream;

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

    @BindView(R.id.settings_switch_db_comparison) Switch mDBSwitch;
    @BindView(R.id.settings_switch_keep_image) Switch mImgSwitch;
    @BindView(R.id.settings_tap_icon_home_currency) ImageView mCurrencyImageView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);

        setSwitchState(mDBSwitch, Settings.DB);
        setSwitchState(mImgSwitch, Settings.IMG);
        setInitialCurrencyIconState();
    }

    /**
     * Loads the previously selected target currency and displays the appropriate icon for the User
     * on Activity creation.
     */
    public void setInitialCurrencyIconState(){
        SharedPreferences settings = getSharedPreferences(Settings.CCA, 0);
        String code = settings.getString(Settings.CUR, "USD");

        setCurrencyIcon(code);
    }

    /**
     * Displays the country of origin of the current currency denomination selected by the user.
     *
     * @param currencyCode  the 3-letter currency code of the current currency
     */
    public void setCurrencyIcon(String currencyCode){
        Bitmap newImage = loadAsset(currencyCode);
        mCurrencyImageView.setImageBitmap(newImage);
    }

    /**
     * Utilizes the current user settings to set the state of the Switch.
     *
     * @param mSwitch   the Switch object to set
     * @param key       the String key needed to access the state of the Switch passed
     */
    public void setSwitchState(Switch mSwitch, String key){
        SharedPreferences settings = getSharedPreferences(Settings.CCA, 0);
        boolean checked = settings.getBoolean(key, false);

        mSwitch.setChecked(checked);
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
                String newCurrency = currencyOptions[currencyPicker.getValue()];
                SharedPreferences settings = getSharedPreferences(Settings.CCA, 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putString(Settings.CUR, newCurrency);
                editor.apply();

                setCurrencyIcon(newCurrency);
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

    /**
     * Helper method that sets common attributes amongst all Dialogs generated in this Activity.
     *
     * @param picker the NumberPicker to be displayed
     * @param array the array of Strings the NumberPicker will display
     */
    public void buildPicker(NumberPicker picker, String [] array){
        picker.setMinValue(0);
        picker.setValue(0);
        picker.setMaxValue(array.length - 1);
        picker.setDisplayedValues(array);
        picker.setEnabled(true);
        picker.setBackgroundColor(getResources().getColor(R.color.colorAccent));
    }

    /**
     * Loads an image asset from Android device memory.
     *
     * @param assetName         the file name of the image asset
     * @return                  a bitmap file of the image asset
     */
    private Bitmap loadAsset(String assetName){
        Log.v(TAG, "Loading asset " + assetName + "...");
        AssetManager manager = getAssets();
        try {
            InputStream imageStream = manager.open("flag_icons/" + assetName + ".png");
            Log.v(TAG, "Asset " + assetName + " loaded successfully!");
            return BitmapFactory.decodeStream(imageStream);
        } catch (IOException e) {
            return null;
        }
    }
}
