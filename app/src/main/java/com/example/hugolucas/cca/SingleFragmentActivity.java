package com.example.hugolucas.cca;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

/**
 * Taken from the Big Nerd Ranch guide to Android programming.
 *
 * Created by hugolucas on 8/26/17.
 */

public abstract class SingleFragmentActivity extends FragmentActivity{

    /**
     * Place fragment that Activity will contain in here.
     *
     * @return Fragment to be hosted in Activity
     */
    protected abstract Fragment createFragment(Bundle savedInstanceState);

    /**
     * Generic onCreate method to put specific fragment into container if none exists within it.
     * @param savedInstanceState Bundle object
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment);
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentById(R.id.fragmentContainer);

        if (fragment == null)
            fragmentManager.beginTransaction()
                    .add(R.id.fragmentContainer, this.createFragment(savedInstanceState))
                    .commit();
    }
}