package com.boetcoin.bitcoinnode.worker.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.boetcoin.bitcoinnode.R;
import com.boetcoin.bitcoinnode.model.Peer;
import com.boetcoin.bitcoinnode.util.Prefs;
import com.boetcoin.bitcoinnode.worker.receiver.ConnectPeersReceiver;
import com.boetcoin.bitcoinnode.worker.thread.GetExternalIpThread;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ross Badenhorst.
 */

public class BitcoinService extends Service {
    public static final String TAG = BitcoinService.class.getSimpleName();

    /**
     * Max number of connections we want to maintain with peers
     */
    public static final int MAX_CONNECTIONS = 8;
    /**
     * How often we want to ping out peers to see if they are still alive.
     */
    public static final int PING_INTERVAL_SECONDS = 60;
    /**
     * List of all the peers we know about.
     */
    private List<Peer> peerList;
    /**
     * Array of connected peers.
     */
    private ArrayList<Peer> connectedPeers = new ArrayList<>(MAX_CONNECTIONS);

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                start();
            }
        }).start();
        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * Starts the bitcoin service.
     */
    private void start() {
        Log.i(TAG, "starting bitcoin service");
        if (!externalIpIsKnown()) {
            new Thread(new GetExternalIpThread(this)).start();
        }

        peerList = getSavedPeers();
        if (peerList.size() == 0) {
            startDnsSeedDiscovery();
        }

        //updateConnectedPeers();

        //if (connectedPeers.size() < MAX_CONNECTIONS) {
            startConnectingToPeers();
        //}
    }

    /**
     * If we know what our external IP is.
     *
     * @return true if yes, false if no.
     */
    private boolean externalIpIsKnown() {
        if (Prefs.getStr(this, Prefs.KEY_EXTERNAL_IP).isEmpty()) {
            return false;
        }

        return true;
    }


    /**
     * Gets peers we have saved in the past.
     * @return - list of locally saved peers.
     */
    private List<Peer> getSavedPeers() {
        return Peer.listAll(Peer.class);
    }

    private void updateConnectedPeers() {
        List<Peer> peerPool  = Peer.listAll(Peer.class);
        connectedPeers.clear();

        for (Peer peer : peerPool) {
            if (peer.connected) {
                connectedPeers.add(peer);
            }
        }
    }

    /**
     * Starts the DNS Seed peer discovery process.
     * We get a list of seeds that are hard coded in to the application.
     * From there we do a lookup to get a list of peers from the seed.
     */
    private void startDnsSeedDiscovery() {
        Log.i(TAG, "startDnsSeedDiscovery");
        String[] dnsSeeds = getResources().getStringArray(R.array.dns_seed_nodes);

        for (String dnsSeed : dnsSeeds) {

            try {
                addPeersFromSeed(dnsSeed);
            } catch (UnknownHostException e) {
                Log.i(TAG, "Failed to get peers from seed: " + dnsSeed);
            }
        }

        Peer.saveInTx(peerList);
    }

    /**
     * Looks up peers from a DNS seed.
     * @param dnsSeed - that we ask for a list of peers.
     * @throws UnknownHostException - When shit happens.
     */
    private void addPeersFromSeed(String dnsSeed) throws UnknownHostException {
        InetAddress[] peersFromDnsSeed = InetAddress.getAllByName(dnsSeed);

        for (InetAddress peerFromDnsSeed : peersFromDnsSeed) {
            peerList.add(new Peer(peerFromDnsSeed.getHostAddress()));
        }
    }

    /**
     * Starts up the peer connection process.
     */
    private void startConnectingToPeers() {
        Log.i(TAG, "startConnectingToPeers");
        Intent connectPeerReceiverIntent = new Intent(this, ConnectPeersReceiver.class);
        connectPeerReceiverIntent.putParcelableArrayListExtra(ConnectPeersReceiver.KEY_CONNECTED_PEERS, connectedPeers);
        sendBroadcast(connectPeerReceiverIntent);
    }
}
