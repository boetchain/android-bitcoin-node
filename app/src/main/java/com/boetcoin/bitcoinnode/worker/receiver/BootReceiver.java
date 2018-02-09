package com.boetcoin.bitcoinnode.worker.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.boetcoin.bitcoinnode.worker.intentservice.PeerCommunicationIntentService;

/**
 * Created by rossbadenhorst on 2018/02/08.
 */

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent peerComsIntent = new Intent(context, PeerCommunicationIntentService.class);
        context.startService(peerComsIntent);
    }
}
