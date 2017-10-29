package com.example.hugolucas.cca;

import android.os.Bundle;
import android.support.v4.app.Fragment;

/**
 * Created by hugolucas on 10/27/17.
 */

public class DebugActivity extends SingleFragmentActivity{

    @Override
    protected Fragment createFragment(Bundle savedInstanceState) {
        return new ExchangeFragment();
    }
}
