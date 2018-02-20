package com.boetchain.bitcoinnode.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.orm.SugarRecord;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Ross Badenhorst.
 */
public class Peer extends SugarRecord implements Comparable<Peer>, Parcelable {

    /**
     * The default port we use when connecting to a peer.
     */
    public static final int DEFAULT_PORT = 8333;
    /**
     * The max amount of peers we want to keep in our poot.
     * We normally connect to 8 peers at a time and each one gives us about 1000 addresses at a time.
     */
    public static final int MAX_POOL_SIZE = 10;

    /**
     * The address of the peer
     */
    public String address;
    /**
     * The port of the peer.
     * Typically 8333.
     */
    public int port = DEFAULT_PORT;
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

    public Peer(String address) {
        this.address = address;
    }

    public Peer(String address, int port, long services) {
        this.address = address;
        this.port = port;
        this.services = services;
        this.connected = false;
    }

    public Peer(String address, int port, long services, long timestamp) {
        this.address = address;
        this.port = port;
        this.services = services;
        this.timestamp = timestamp;
        this.connected = false;
    }

    /**
     * Gets the entire peer pool.
     * @return - entire peer pool.
     */
    public static List<Peer> getPeerPool() {
        List<Peer> pool = Peer.listAll(Peer.class);
        Collections.sort(pool);

        return pool;
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
     * @param peers - that we want to remember.
     */
    public static void addPeersToPool(List<Peer> peers) {
        // Add the pool into the incoming array
        peers.addAll(Peer.getPeerPool());

        // Remove dups from the peer pool
        for(int i = 0; i <peers.size();i++){
            for(int j=i+1;j<peers.size();j++){
                if(peers.get(i).equals(peers.get(j))){
                    peers.remove(j);
                    j--;
                }
            }
        }

        // trim the pool if need be
        Collections.sort(peers);
        if (peers.size() > MAX_POOL_SIZE) {
            peers = peers.subList(0 , MAX_POOL_SIZE);
        }

        Peer.deleteAll(Peer.class);
        Peer.saveInTx(peers);
    }

    /**
     * Gets the peer pool and trims it if we have more peers
     * then the max amount.
     */
    public static void forgetOldPeers() {
        List<Peer> pool = getPeerPool();

        if (pool.size() > MAX_POOL_SIZE) {
            List<Peer> peersToDelete = pool.subList(MAX_POOL_SIZE, pool.size());
            Peer.deleteInTx(peersToDelete);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (!Peer.class.isAssignableFrom(obj.getClass())) {
            return false;
        }

        final Peer other = (Peer) obj;
        if (this.address.equalsIgnoreCase(other.address)) {
            return true;
        }

        return false;
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

        return (int) (peer.timestamp - this.timestamp);
    }
}
