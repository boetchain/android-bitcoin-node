package com.boetcoin.bitcoinnode.model;

import android.os.AsyncTask;
import android.util.Log;

import com.boetcoin.bitcoinnode.App;
import com.orm.SugarRecord;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by rossbadenhorst on 2018/01/31.
 */

public class Peer extends SugarRecord {

    /**
     * The IP address of the peer
     */
    public String ip;

    public Peer() {
    }

    public Peer(String ip) {
        this.ip = ip;
    }

    public static void findByDnsSeeds(final String[] dnsSeeds) {
        Log.i(App.TAG, "findByDnsSeeds: " + dnsSeeds.length);

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                List<Peer> peers = new ArrayList<>();

                try {
                    for (String dnsSeed : dnsSeeds) {
                        InetAddress[] peersFromDnsSeed = InetAddress.getAllByName(dnsSeed);

                        for (InetAddress peerFromDnsSeed : peersFromDnsSeed) {
                            peers.add(new Peer(peerFromDnsSeed.getHostAddress()));
                        }
                    }

                    Log.i(App.TAG, "Found: " + peers.size() + " peers");

                    Peer.deleteAll(Peer.class);
                    Peer.saveInTx(peers);
                } catch (Exception e) {

                }
            }
        });
    }
}
