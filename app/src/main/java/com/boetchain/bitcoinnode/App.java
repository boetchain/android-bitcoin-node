package com.boetchain.bitcoinnode;

import android.util.Log;

/**
 * Created by rossbadenhorst on 2018/01/31.
 */

public class App extends com.orm.SugarApp {
    public static final String TAG = "bitcoinnode";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TAG, "Starting...");
    }
}
