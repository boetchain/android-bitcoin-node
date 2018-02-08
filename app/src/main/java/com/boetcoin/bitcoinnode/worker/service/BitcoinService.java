package com.boetcoin.bitcoinnode.worker.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.boetcoin.bitcoinnode.App;
import com.boetcoin.bitcoinnode.util.Prefs;
import com.boetcoin.bitcoinnode.worker.thread.GetExternalIpThread;

/**
 * Created by rossbadenhorst on 2018/02/08.
 */

public class BitcoinService extends Service {
    public static final String TAG = BitcoinService.class.getSimpleName();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        start();
        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void start() {
        Log.i(App.TAG, "start");
        if (!externalIpIsKnown()) {
            new Thread(new GetExternalIpThread(this)).start();
        }


    }

    /**
     * If we know what our external IP is.
     *
     * @return true if yes, false if no.
     */
    private boolean externalIpIsKnown() {
        if (Prefs.getStr(this, Prefs.KEY_EXTERNAL_IP).isEmpty()) {
            return false;
        }

        return true;
    }
}
