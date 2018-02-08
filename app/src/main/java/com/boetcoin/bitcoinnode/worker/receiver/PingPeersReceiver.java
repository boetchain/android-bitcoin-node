package com.boetcoin.bitcoinnode.worker.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by Ross Badenhorst.
 */
public class PingPeersReceiver extends BroadcastReceiver {
    public static final String TAG = PingPeersReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "PingPeersReceiver: onReceive");
    }
}
