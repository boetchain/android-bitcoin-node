package com.boetchain.bitcoinnode;

import android.util.Log;

/**
 * Created by rossbadenhorst on 2018/01/31.
 */

public class App extends com.orm.SugarApp {
    public static final String TAG = "bitcoinnode";

    /**
     * True if the app is currently open and in the foreground.
     * Set true/false in the onPostResume/onPause methods in
     * BaseActivity
     */
    public static boolean isOpen = false;

    /**
     * If the PeerChatActivity is open this String get set to that peer's IP. That way
     * broadcasts can be sent for that one peer.
     */
    public static String monitoringPeerIP = "";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TAG, "Starting...");
    }
}
