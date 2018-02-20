package com.boetchain.bitcoinnode.worker.thread;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.boetchain.bitcoinnode.R;
import com.boetchain.bitcoinnode.model.Peer;
import com.boetchain.bitcoinnode.util.Lawg;
import com.boetchain.bitcoinnode.worker.broadcaster.PeerBroadcaster;
import com.boetchain.bitcoinnode.worker.service.PeerManagementService;

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
        this.context = context;
    }

    @Override
    public void runThread() {
        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(PeerManagementService.ACTION_DNS_SEED_DISCOVERY_STARTING));

        ArrayList<Peer> peersFromDnsSeeds = startDnsSeedDiscovery();

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Intent dnsSeedDiscoveryCompleteIntent = new Intent(PeerManagementService.ACTION_DNS_SEED_DISCOVERY_COMPLETE);
        dnsSeedDiscoveryCompleteIntent.putParcelableArrayListExtra(PeerBroadcaster.KEY_PEERS, peersFromDnsSeeds);
        LocalBroadcastManager.getInstance(context).sendBroadcast(dnsSeedDiscoveryCompleteIntent);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Starts the DNS Seed peer discovery process.
     * We get a list of seeds that are hard coded in to the application.
     * From there we do a lookup to get a list of peers from the seed.
     */
    private ArrayList<Peer> startDnsSeedDiscovery() {
        Lawg.i("startDnsSeedDiscovery");
        String[] dnsSeeds = context.getResources().getStringArray(R.array.dns_seed_nodes);
        ArrayList<Peer> peerList = new ArrayList<>();

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
