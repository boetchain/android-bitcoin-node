package com.boetchain.bitcoinnode.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.boetchain.bitcoinnode.R;
import com.orm.SugarRecord;
import com.orm.dsl.Ignore;

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
     * The max amount of peers we want to keep in our pool.
     * We normally connect to 8 peers at a time and each one gives us about 1000 addresses at a time.
     */
    public static final int MAX_POOL_SIZE = 40;
    /**
     * The max amount of historical chat message we want to store against each peer.
     * We limit this, because it serves no other function besides some fancy UI.
     *
     */
    public static final int MAX_CHAT_HISTORY_SIZE = 50;
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
    /**
     * Which country the peer is in.
     * Calculated from ip.
     */
    public String country = "";
    /**
     * Which city the peer is in.
     * Calculated from ip.
     */
    public String city = "";
    /**
     * What the country code the peer is in.
     * Calculated from ip.
     */
    public String countryCode;
    /**
     * The latitude of the peer.
     * Calculated from ip.
     */
    public double lat;
    /**
     * The longitude of the peer.
     * Calculated from ip.
     */
    public double lng;
    /**
     * The ISP the peer uses.
     * Calculated from ip.
     */
    public String isp;
    /**
     * State or province "code".
     * Calculated from ip.
     */
    public String region;
    /**
     * Sate or province name.
     * Calculated from ip.
     */
    public String regionName;

    /**
     * Stores a historical list of chats between ourself and this peer.
     * We limit this to the MAX_CHAT_HISTORY_SIZE.
     *
     * We ignore this, because we dont want to waste time persisting
     * messages or history to the DB.
     */
    @Ignore
    private ArrayList<ChatLog> chatHistory;

    public Peer() {
        chatHistory = new ArrayList<>();
    }

    public Peer(String address) {
        this.address = address;
        chatHistory = new ArrayList<>(MAX_CHAT_HISTORY_SIZE);
    }

    public Peer(String address, int port, long services) {
        this.address = address;
        this.port = port;
        this.services = services;
        this.connected = false;
        chatHistory = new ArrayList<>(MAX_CHAT_HISTORY_SIZE);
    }

    public Peer(String address, int port, long services, long timestamp) {
        this.address = address;
        this.port = port;
        this.services = services;
        this.timestamp = timestamp;
        this.connected = false;
        chatHistory = new ArrayList<>(MAX_CHAT_HISTORY_SIZE);
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
     * Adds a chatlog to the history.
     * Shit cans old chat messages, if need be.
     * @param chatLog - new chat to store.
     */
    public void appendChatHistory(ChatLog chatLog) {
        chatHistory.add(chatLog);

        // trim the message history if need be
        Collections.sort(chatHistory);
        if (chatHistory.size() > MAX_CHAT_HISTORY_SIZE) {
            chatHistory.remove(0);
        }
    }

    /**
     * Gets the chat history for a peer.
     * By now the list should be sorted and in order
     * so we can simply pump it to the UI.
     * @return - chat histor for a peer.
     */
    public ArrayList<ChatLog> getChatHistory() {
        return this.chatHistory;
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

    @Override
    public int compareTo(@NonNull Peer peer) {

        return (int) (peer.timestamp - this.timestamp);
    }

    protected Peer(Parcel in) {
        address = in.readString();
        port = in.readInt();
        services = in.readLong();
        timestamp = in.readLong();
        connected = in.readByte() != 0x00;
        country = in.readString();
        city = in.readString();
        countryCode = in.readString();
        lat = in.readDouble();
        lng = in.readDouble();
        isp = in.readString();
        region = in.readString();
        regionName = in.readString();
        if (in.readByte() == 0x01) {
            chatHistory = new ArrayList<ChatLog>();
            in.readList(chatHistory, ChatLog.class.getClassLoader());
        } else {
            chatHistory = null;
        }
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
        dest.writeString(country);
        dest.writeString(city);
        dest.writeString(countryCode);
        dest.writeDouble(lat);
        dest.writeDouble(lng);
        dest.writeString(isp);
        dest.writeString(region);
        dest.writeString(regionName);
        if (chatHistory == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(chatHistory);
        }
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
}
