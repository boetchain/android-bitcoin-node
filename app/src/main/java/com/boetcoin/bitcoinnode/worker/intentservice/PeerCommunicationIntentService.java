package com.boetcoin.bitcoinnode.worker.intentservice;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;

import com.boetcoin.bitcoinnode.R;
import com.boetcoin.bitcoinnode.model.Peer;
import com.boetcoin.bitcoinnode.util.Lawg;
import com.boetcoin.bitcoinnode.worker.thread.PeerCommunicatorThread;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ross Badenhorst.
 */
public class PeerCommunicationIntentService extends IntentService {
    public static final String TAG = PeerCommunicationIntentService.class.getSimpleName();

    /**
     * Max number of connections we want to maintain with peers
     */
    public static final int MAX_CONNECTIONS = 1;

    public Context context;

    public PeerCommunicationIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Lawg.i("Starting PeerCommunicationIntentService...");

        context = this;
        List<Peer> peerPool = Peer.getPeerPool();
        List<Peer> connectedPeers = Peer.getConnectedPeers();

        if (peerPool.size() == 0) {
            peerPool = startDnsSeedDiscovery();
        }

        Lawg.i("Connecting to " + connectedPeers.size() + " existing connections");
        for (int i = 0; i < connectedPeers.size() && i < MAX_CONNECTIONS; i++)  {
            new Thread(new PeerCommunicatorThread(context, connectedPeers.get(i))).start();
        }


        if (connectedPeers.size() < MAX_CONNECTIONS) {
            int numberOfConnectionsNeeded = MAX_CONNECTIONS - connectedPeers.size();
            Lawg.i("Starting " + numberOfConnectionsNeeded + " new connections");
            for (int i = 0; i < numberOfConnectionsNeeded; i++) {
                Peer peerToConnectTo = findPeerToConnectTo();
                new Thread(new PeerCommunicatorThread(context, peerToConnectTo)).start();
            }
        }
    }

    /**
     * Starts the DNS Seed peer discovery process.
     * We get a list of seeds that are hard coded in to the application.
     * From there we do a lookup to get a list of peers from the seed.
     */
    private List<Peer> startDnsSeedDiscovery() {
        Lawg.i("startDnsSeedDiscovery");
        String[] dnsSeeds = context.getResources().getStringArray(R.array.dns_seed_nodes);
        List<Peer> peerList = new ArrayList<>();

        for (String dnsSeed : dnsSeeds) {
            try {
                addPeersFromSeed(dnsSeed, peerList);
            } catch (UnknownHostException e) {
                Lawg.i("Failed to get peers from seed: " + dnsSeed);
            }
        }

        Peer.saveInTx(peerList);
        return peerList;
    }

    /**
     * Looks up peers from a DNS seed.
     * @param dnsSeed - that we ask for a list of peers.
     * @throws UnknownHostException - When shit happens.
     */
    private void addPeersFromSeed(String dnsSeed, List<Peer> peerList ) throws UnknownHostException {
        InetAddress[] peersFromDnsSeed = InetAddress.getAllByName(dnsSeed);

        for (InetAddress peerFromDnsSeed : peersFromDnsSeed) {
            peerList.add(new Peer(peerFromDnsSeed.getHostAddress()));
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
        //Lawg.i("findPeerToConnectTo");
        List<Peer> peerPool = Peer.listAll(Peer.class);
        if (peerPool.size() == 0) {
            peerPool = startDnsSeedDiscovery();
        }

        for (int i = 0; i < peerPool.size(); i++) {
            Peer peer  = peerPool.get(i);
            if (!peer.connected) {
                return peer;
            }
        }

        return null;
    }
}
