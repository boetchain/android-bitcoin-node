package com.boetcoin.bitcoinnode;

import android.app.Application;
import android.util.Log;

/**
 * Created by rossbadenhorst on 2018/01/31.
 */

public class App extends Application {
    public static final String TAG = "bitcoinnode";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TAG, "Starting...");
    }
}
