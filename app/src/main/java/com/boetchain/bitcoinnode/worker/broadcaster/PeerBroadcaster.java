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
    
    public void broadcast(String action) {
        
        Intent disconnectedPeerIntent = new Intent(action);
        disconnectedPeerIntent.putExtra(PeerBroadcaster.KEY_PEER, peer);
        LocalBroadcastManager.getInstance(context).sendBroadcast(disconnectedPeerIntent);
    }

    public void broadcastLog(String text, String command, int type) {

        if (App.monitoringPeerIP.equals(peer.address)) {

            broadcastLogAll(text, command, type);
        }
    }

    public void broadcastLog(String text, int type) {

        if (App.monitoringPeerIP.equals(peer.address)) {

            broadcastLogAll(text, type);
        }
    }

    public void broadcastLogAll(String text, String command, int type) {

        Intent disconnectedPeerIntent = new Intent();
        disconnectedPeerIntent.setAction(PeerChatActivity.getBroadcastAction());
        disconnectedPeerIntent.putExtra(PeerChatActivity.EXTRA_TEXT, text);
        disconnectedPeerIntent.putExtra(PeerChatActivity.EXTRA_COMMAND, command);
        disconnectedPeerIntent.putExtra(PeerChatActivity.EXTRA_TIME, System.currentTimeMillis());
        disconnectedPeerIntent.putExtra(PeerChatActivity.EXTRA_TYPE, type);
        context.sendBroadcast(disconnectedPeerIntent);
    }

    public void broadcastLogAll(String text, int type) {

        broadcastLogAll(text, "", type);
    }
}
