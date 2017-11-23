package com.example.hugolucas.cca;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.CompoundButton;

import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;

/**
 * Handles the logic behind saving user settings.
 *
 * Created by hugolucas on 11/22/17.
 */

public class SettingsActivity extends AppCompatActivity{

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.settings_view_history_interval)
    public void setHistoryInterval(){

    }

    @OnClick(R.id.settings_view_home_currency)
    public void setHomeCurrency(){

    }

    @OnClick(R.id.settings_view_search_radius)
    public void setSearchRadius(){

    }

    @OnCheckedChanged(R.id.settings_switch_keep_image)
    public void keepImage(CompoundButton button, boolean checked){

    }
}
