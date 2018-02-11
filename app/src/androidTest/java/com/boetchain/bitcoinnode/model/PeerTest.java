package com.boetchain.bitcoinnode.model;

import android.support.test.runner.AndroidJUnit4;

import com.boetchain.bitcoinnode.util.Lawg;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by Ross Badenhorst.
 */
@RunWith(AndroidJUnit4.class)
public class PeerTest {

    @Before
    public void setUp() throws Exception {
        Peer.deleteAll(Peer.class);
    }

    @After
    public void tearDown() throws Exception {
        Peer.deleteAll(Peer.class);
    }

    @Test
    public void newestPeerIsFirst() throws Exception {
        Lawg.i( "newestPeerIsFirst");
        int numPeersToAdd = 10;
        addPeers(numPeersToAdd);

        List<Peer> pool = Peer.getPeerPool();
        assertEquals(numPeersToAdd, pool.size());

        for (int i = 1; i < pool.size(); i++) {
            Peer prevPeer = pool.get(i - 1);
            Peer currentPeer = pool.get(i);

            assertTrue(prevPeer.timestamp <= currentPeer.timestamp);
        }
    }

    @Test
    public void maxPoolSizeMaintained() throws Exception {
        int numPeersToAdd = Peer.MAX_POOL_SIZE + 1000;
        addPeers(numPeersToAdd);

        List<Peer> pool = Peer.getPeerPool();

        Lawg.d(Peer.MAX_POOL_SIZE + " | " + pool.size());
        assertEquals(Peer.MAX_POOL_SIZE, pool.size());
    }

    @Test
    public void noDuplicatePeersAreSaved() throws Exception {
        // Add duplicates
        int numDuplicatePeersToAdd = 10;
        ArrayList<Peer> duplicatePeers = new ArrayList<>();
        for (int i = 0; i < numDuplicatePeersToAdd; i++) {
            Peer peer = new Peer();
            peer.address = "1"; // The address makes the peer unique
            peer.timestamp = new Random().nextInt(numDuplicatePeersToAdd);

            duplicatePeers.add(peer);
        }
        Peer.addPeersToPool(duplicatePeers);

        // Add some uniques
        int numOfUniquePeersToAdd = 10;
        addPeers(numOfUniquePeersToAdd);

        List<Peer> pool = Peer.getPeerPool();
        Lawg.i((numOfUniquePeersToAdd + 1) + " | " + pool.size());
        assertEquals(numOfUniquePeersToAdd + 1, pool.size());
    }

    private void addPeers(int numberOfPeersToAdd) {
        List<Peer> peers = new ArrayList<>();
        for (int i = 0; i < numberOfPeersToAdd; i++) {
            Peer peer = new Peer();
            peer.address = new Random().nextInt(numberOfPeersToAdd) + ""; // Different addr = different peer
            peer.timestamp = new Random().nextInt(numberOfPeersToAdd);
            peers.add(peer);
        }
        Peer.addPeersToPool(peers);
    }
}
