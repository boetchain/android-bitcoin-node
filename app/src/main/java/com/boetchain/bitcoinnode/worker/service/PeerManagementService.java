package com.boetchain.bitcoinnode.worker.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

import com.boetchain.bitcoinnode.App;
import com.boetchain.bitcoinnode.model.ChatLog;
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
    /**
     * All the peers we currently have in the pool.
     */
    private List<Peer> peerPool;
    /**
     * Used to see if the service is active or now.
     */
    private boolean isRunning;
    /**
     * Binder given to clients
     */
    private final IBinder binder = new LocalBinder();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Lawg.u(this, new Peer(App.monitoringPeerIP), "Bitcoin Service Starting...", ChatLog.TYPE_NEUTRAL);
        isRunning = true;

        LocalBroadcastManager.getInstance(this).registerReceiver(localBroadcastReceiver, new IntentFilter(ACTION_DNS_SEED_DISCOVERY_COMPLETE));
        LocalBroadcastManager.getInstance(this).registerReceiver(localBroadcastReceiver, new IntentFilter(ACTION_PEER_CONNECTED));
        LocalBroadcastManager.getInstance(this).registerReceiver(localBroadcastReceiver, new IntentFilter(ACTION_PEER_DISCONNECTED));

        peerPool = Peer.getPeerPool();
        if (peerPool.size() == 0) {
            startDnsSeedDiscovery();
        } else {
            disconnectFromPeers();
            connectToPeers();
        }

        return START_STICKY;
    }

    /**
     * Often if the app is closed or crashes, the peers get saved in a connected state.
     * Just to be sure when the app starts we make all the peers disconnected.
     */
    private void disconnectFromPeers() {
        for (Peer peer : peerPool)
            if (peer.connected) {
                peer.connected = false;
                peer.save();
            }
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
            if (peerToConnectTo != null) {
                new Thread(new PeerCommunicatorThread(this, peerToConnectTo)).start();
            } else {
                Lawg.e("No peers to connect to...");
            }
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
        peerPool = Peer.getPeerPool();

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

        disconnectFromPeers();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(localBroadcastReceiver);

        isRunning = false;
        Lawg.u(this, new Peer(App.monitoringPeerIP), "Bitcoin Service Shutting down...", ChatLog.TYPE_NEUTRAL);
    }

    /**
     * Clients can call this to see if the service is running or not.
     * @return true if yes, false if not.
     */
    public boolean isRunning() {
        return this.isRunning;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
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
                if (numberOfActiveConnections > 0) {
                    numberOfActiveConnections--;// Only minus connects if we have any...
                }
                Lawg.i("Peer disconnected: " + numberOfActiveConnections);
                connectToPeers();
            }
        }
    };

    /**
     *
     */
    public class LocalBinder extends Binder {
        public PeerManagementService getService() {
            return PeerManagementService.this;
        }
    }
}
