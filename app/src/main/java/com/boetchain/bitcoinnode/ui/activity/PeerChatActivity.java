package com.boetchain.bitcoinnode.ui.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.boetchain.bitcoinnode.App;
import com.boetchain.bitcoinnode.R;
import com.boetchain.bitcoinnode.model.Peer;

/**
 * Created by Tyler Hogarth on 2018/02/10.
 */

public class PeerChatActivity extends BaseActivity {

    private Peer peer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_peer_chat);
    }

    @Override
    protected void onPause() {
        super.onPause();
        App.monitoringPeerIP = peer.ip;
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();

        App.monitoringPeerIP = "";
    }
}
