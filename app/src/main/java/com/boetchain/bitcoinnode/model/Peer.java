package com.boetchain.bitcoinnode.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.orm.SugarRecord;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rossbadenhorst on 2018/01/31.
 */

public class Peer extends SugarRecord implements Comparable<Peer>, Parcelable {

    /**
     * The IP address of the peer
     */
    public String address;
    /**
     * The port of the peer.
     * Typically 8333.
     */
    public int port;
    /**
     * The services the peer is providing.
     */
    public long services;
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

    public Peer(String address, int port, long services) {
        this.address = address;
        this.port = port;
        this.services = services;
        this.connected = false;
    }

    /**
     * Gets the entire peer pool.
     * @return - entire peer pool.
     */
    public static List<Peer> getPeerPool() {
        return Peer.listAll(Peer.class);
    }

    /**
     * Gets all the peers we are currently trying to maintain connections with.
     * @return only the connected peers.
     */
    public static ArrayList<Peer> getConnectedPeers() {
        List<Peer> peerPool  = Peer.listAll(Peer.class);
        ArrayList<Peer> connectedPeers = new ArrayList<>();

        for (Peer peer : peerPool) {
            if (peer.connected) {
                connectedPeers.add(peer);
            }
        }

        return connectedPeers;
    }

    /**
     * Adds peers to the peer pool.
     * TODO stop duplicates
     * TODO limit pool size, removing older peers
     * @param newPeers - that we want to remember.
     */
    public static void addPeersToPool(List<Peer> newPeers) {
        Peer.saveInTx(newPeers);
    }

    protected Peer(Parcel in) {
        address = in.readString();
        port = in.readInt();
        services = in.readLong();
        timestamp = in.readLong();
        connected = in.readByte() != 0x00;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(address);
        dest.writeInt(port);
        dest.writeLong(services);
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
