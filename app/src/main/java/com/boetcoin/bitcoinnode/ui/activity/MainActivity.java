package com.boetcoin.bitcoinnode.ui.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.boetcoin.bitcoinnode.App;
import com.boetcoin.bitcoinnode.R;
import com.boetcoin.bitcoinnode.model.Message.VersionMessage;
import com.boetcoin.bitcoinnode.model.Peer;
import com.boetcoin.bitcoinnode.util.Util;
import com.google.common.primitives.Longs;

import java.nio.ByteBuffer;
import java.util.List;

public class MainActivity extends AppCompatActivity {




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        List<Peer> locallySavedPeers = Peer.listAll(Peer.class);
        Log.i(App.TAG, "locallySavedPeers: " + locallySavedPeers.size());

        if (locallySavedPeers.size() == 0) {
            Peer.findByDnsSeeds(getResources().getStringArray(R.array.dns_seed_nodes));
        } else {
            VersionMessage versionMessage = new VersionMessage();
            Log.i(App.TAG, versionMessage.toString());
        }
    }
}
