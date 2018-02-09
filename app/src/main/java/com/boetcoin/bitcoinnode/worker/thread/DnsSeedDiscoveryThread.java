package com.boetcoin.bitcoinnode.worker.thread;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.boetcoin.bitcoinnode.R;
import com.boetcoin.bitcoinnode.model.Peer;
import com.boetcoin.bitcoinnode.util.Lawg;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ross Badenhorst.
 */
public class DnsSeedDiscoveryThread extends BaseThread {

    private Context context;

    public DnsSeedDiscoveryThread(Context context) {
        super(context);
    }

    @Override
    public void run() {
        startDnsSeedDiscovery();

        Intent dnsDiscoceryCompleteIntent = new Intent();
        LocalBroadcastManager.getInstance(context).sendBroadcastSync(dnsDiscoceryCompleteIntent);
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
}
