package com.boetchain.bitcoinnode.worker.broadcaster;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.boetchain.bitcoinnode.App;
import com.boetchain.bitcoinnode.model.Peer;
import com.boetchain.bitcoinnode.ui.activity.PeerChatActivity;

/**
 * Created by Tyler Hogarth on 2018/02/14.
 * 
 * This class will broadcast any messages that relate to Peers
 */

public class PeerBroadcaster {

    /**
     * When we try and connect to a peer.
     */
    public static final String ACTION_PEER_CONNECTION_ATTEMPT = "ACTION_PEER_CONNECTION_ATTEMPT";
    /**
     * When the connection is successfull and we can now make comms with the peer.
     */
    public static final String ACTION_PEER_CONNECTED = "ACTION_PEER_CONNECTED";
    /**
     * Something about a single peer has changed or is updated.
     */
    public static final String ACTION_PEER_UPDATED = "ACTION_PEER_UPDATED";
    /**
     * When we have disconnected from a peer.
     */
    public static final String ACTION_PEER_DISCONNECTED = "ACTION_PEER_DISCONNECTED";

    public static final String KEY_PEER = "KEY_PEER";
    public static final String KEY_PEERS = "KEY_PEERS";
    
    private Context context;
    private Peer peer;
    
    public PeerBroadcaster(Context context, Peer peer) {
        this.context = context;
        this.peer = peer;
    }

    /**
     * Broadcasts the peer based on the actions.
     * @param action
     */
    public void broadcast(String action) {
        Intent disconnectedPeerIntent = new Intent(action);
        disconnectedPeerIntent.putExtra(PeerBroadcaster.KEY_PEER, peer);
        LocalBroadcastManager.getInstance(context).sendBroadcast(disconnectedPeerIntent);
    }
}
