package com.example.hugolucas.cca;

import android.os.Bundle;
import android.support.v4.app.Fragment;

/**
 * Created by hugolucas on 10/27/17.
 */

public class HostActivity extends SingleFragmentActivity{

    private static Fragment mFragment;

    public static void setFragment(Fragment fragment){
        mFragment = fragment;
    }

    @Override
    protected Fragment createFragment(Bundle savedInstanceState) {
        return mFragment;
    }
}
