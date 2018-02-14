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

    public static final String ACTION_PEER_CONNECTION_ATTEMPT = "ACTION_PEER_CONNECTION_ATTEMPT";
    public static final String ACTION_PEER_CONNECTED = "ACTION_PEER_CONNECTED";
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

    public void broadcastLog(String msg, int type) {

        if (App.monitoringPeerIP.equals(peer.address)) {

            broadcastLogAll(msg, type);
        }
    }

    public void broadcastLogAll(String msg, int type) {

        Intent disconnectedPeerIntent = new Intent();
        disconnectedPeerIntent.setAction(PeerChatActivity.getBroadcastAction());
        disconnectedPeerIntent.putExtra(PeerChatActivity.EXTRA_MSG, msg);
        disconnectedPeerIntent.putExtra(PeerChatActivity.EXTRA_TYPE, type);
        context.sendBroadcast(disconnectedPeerIntent);
    }
}
