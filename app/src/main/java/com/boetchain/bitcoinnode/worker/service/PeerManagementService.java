package com.boetchain.bitcoinnode.worker.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

import com.boetchain.bitcoinnode.model.Peer;
import com.boetchain.bitcoinnode.util.Lawg;
import com.boetchain.bitcoinnode.worker.thread.DnsSeedDiscoveryThread;
import com.boetchain.bitcoinnode.worker.thread.PeerCommunicatorThread;

import java.util.List;

/**
 * Created by Ross Badenhorst.
 */
public class PeerManagementService extends Service {

    public static final String ACTION_DNS_SEED_DISCOVERY_COMPLETE   = "ACTION_DNS_SEED_DISCOVERY_COMPLETE";
    public static final String ACTION_PEER_CONNECTED                = "ACTION_PEER_CONNECTED";
    public static final String ACTION_PEER_DISCONNECTED             = "ACTION_PEER_DISCONNECTED";

    /**
     * Max number of connections we want to maintain with peers
     */
    public static final int MAX_CONNECTIONS = 1;
    /**
     * How many connections are currently active.
     */
    private int numberOfActiveConnections = 0;
    private List<Peer> peerPool;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Lawg.i("Bitcoin Service Starting...");

        LocalBroadcastManager.getInstance(this).registerReceiver(localBroadcastReceiver, new IntentFilter(ACTION_DNS_SEED_DISCOVERY_COMPLETE));
        LocalBroadcastManager.getInstance(this).registerReceiver(localBroadcastReceiver, new IntentFilter(ACTION_PEER_CONNECTED));
        LocalBroadcastManager.getInstance(this).registerReceiver(localBroadcastReceiver, new IntentFilter(ACTION_PEER_DISCONNECTED));

        peerPool = Peer.getPeerPool();
        if (peerPool.size() == 0) {
            startDnsSeedDiscovery();
        } else {
            connectToPeers();
        }

        return START_STICKY;
    }

    /**
     * Starts the look up process to find initial peers or seeds to connect to.
     */
    private void startDnsSeedDiscovery() {
        Lawg.i("startDnsSeedDiscovery");
        new Thread(new DnsSeedDiscoveryThread(this)).start();
    }

    /**
     * Starts threads to start connecting to peers.
     */
    private void connectToPeers() {
        int numberOfConnectionsNeeded = MAX_CONNECTIONS - numberOfActiveConnections;
        Lawg.i("Starting " + numberOfConnectionsNeeded + " new connections");
        for (int i = 0; i < numberOfConnectionsNeeded; i++) {
            Peer peerToConnectTo = findPeerToConnectTo();
            new Thread(new PeerCommunicatorThread(this, peerToConnectTo)).start();
        }
    }

    /**
     * Finds a peer to connect to.
     * Gets all the peers that we don't have a connection with,
     * then sorts them by the most recent peers first.
     * (We assume a peer we recently spoke to is probably a guy to speak to again).
     *
     * @return - a peer we can connect to.
     */
    private Peer findPeerToConnectTo() {
        for (int i = 0; i < peerPool.size(); i++) {
            Peer peer  = peerPool.get(i);
            if (!peer.connected) {
                return peer;
            }
        }

        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        LocalBroadcastManager.getInstance(this).unregisterReceiver(localBroadcastReceiver);
        Lawg.i("Bitcoin Service Shutting down...");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * Listens for broadcasts from other parts of the app.
     */
    private BroadcastReceiver localBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equalsIgnoreCase(ACTION_DNS_SEED_DISCOVERY_COMPLETE)) {
                connectToPeers();
            }

            if (intent.getAction().equalsIgnoreCase(ACTION_PEER_CONNECTED)) {
                numberOfActiveConnections++;
                Lawg.i("Peer connected: " + numberOfActiveConnections);
            }

            if (intent.getAction().equalsIgnoreCase(ACTION_PEER_DISCONNECTED)) {
                numberOfActiveConnections--;
                Lawg.i("Peer disconnected: " + numberOfActiveConnections);
                connectToPeers();
            }
        }
    };
}
