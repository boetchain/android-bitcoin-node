package com.boetcoin.bitcoinnode.worker.receiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by rossbadenhorst on 2018/02/08.
 */

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        startBitcoinService(context);
    }

    /**
     * Starts the bitcoin service.
     * @param context - of the app.
     */
    private void startBitcoinService(Context context) {
        Intent pingPeersReceiverIntent = new Intent(context, PeerConnectionCheckReceiver.class);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 0, pingPeersReceiverIntent, 0);
        AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmMgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, 0, PeerConnectionCheckReceiver.CHECK_INTERVAL_SECONDS * 1000, alarmIntent);
    }
}
