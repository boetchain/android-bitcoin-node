package com.boetcoin.bitcoinnode;

import android.content.Intent;
import android.util.Log;

import com.boetcoin.bitcoinnode.worker.intentservice.PeerCommunicationIntentService;

/**
 * Created by rossbadenhorst on 2018/01/31.
 */

public class App extends com.orm.SugarApp {
    public static final String TAG = "bitcoinnode";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TAG, "Starting...");

        Intent peerComsIntent = new Intent(this, PeerCommunicationIntentService.class);
        startService(peerComsIntent);
    }
}
