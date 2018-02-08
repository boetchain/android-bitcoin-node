package com.boetcoin.bitcoinnode.model;

import android.os.AsyncTask;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.util.Log;

import com.boetcoin.bitcoinnode.App;
import com.orm.SugarRecord;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by rossbadenhorst on 2018/01/31.
 */

public class Peer extends SugarRecord implements Parcelable, Comparable<Peer> {

    /**
     * The IP address of the peer
     */
    public String ip;
    /**
     * When last we made contact with a peer
     */
    public long timestamp;
    /**
     * If we believe we have a connection to this peer
     */
    public boolean connected;

    public Peer() {
    }

    public Peer(String ip) {
        this.ip = ip;
        this.connected = false;
    }

    /**
     * Gets peers from the dnsSeeds.
     * Used to boot up the node, if it doesn't know any one - Used for first run.
     * @param dnsSeeds - dnsSeeds hardcoded into the application - See @values/arrays.xml
     */
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

    protected Peer(Parcel in) {
        ip = in.readString();
        timestamp = in.readLong();
        connected = in.readByte() != 0x00;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(ip);
        dest.writeLong(timestamp);
        dest.writeByte((byte) (connected ? 0x01 : 0x00));
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Peer> CREATOR = new Parcelable.Creator<Peer>() {
        @Override
        public Peer createFromParcel(Parcel in) {
            return new Peer(in);
        }

        @Override
        public Peer[] newArray(int size) {
            return new Peer[size];
        }
    };

    @Override
    public int compareTo(@NonNull Peer peer) {
        return (int) this.timestamp - (int) peer.timestamp;
    }
}
