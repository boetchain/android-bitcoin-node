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
import com.boetchain.bitcoinnode.worker.broadcaster.PeerBroadcaster;
import com.boetchain.bitcoinnode.worker.thread.DnsSeedDiscoveryThread;
import com.boetchain.bitcoinnode.worker.thread.PeerCommunicatorThread;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ross Badenhorst.
 */
public class PeerManagementService extends Service {

    public static final String ACTION_DNS_SEED_DISCOVERY_STARTING   = "ACTION_DNS_SEED_DISCOVERY_STARTING";
    public static final String ACTION_DNS_SEED_DISCOVERY_COMPLETE   = "ACTION_DNS_SEED_DISCOVERY_COMPLETE";
    public static final String ACTION_SERVICE_STARTED               = "ACTION_SERVICE_STARTED";
    public static final String ACTION_SERVICE_DESTROYED             = "ACTION_SERVICE_DESTROYED";

    /**
     * Max number of connections we want to maintain with peers
     */
    public static final int MAX_CONNECTIONS = 4;

	/**
	 * The amount of time that needs to pass before this service will act on another start command.
	 */
	public static final int START_DELAY = 1000 * 3;

    /**
     * All the peers we currently have in the pool.
     */
    private List<Peer> peerPool = new ArrayList<>();
    /**
     * A timestamp that represent when this service was started last to prevent onStartCommand being
     * called too many times.
     * NOTE: If 0, this service is not running
     */
    private long startedRunningAt;
    /**
     * Binder given to clients
     */
    private final IBinder binder = new LocalBinder();

    private DnsSeedDiscoveryThread dnsSeedDiscoveryThread;

    private List<PeerCommunicatorThread> peerCommunicatorThreads = new ArrayList();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Lawg.i("onStartCommand");

        //We want to limit how many successive calls to on start command there are
        if (startedRunningAt < System.currentTimeMillis() + START_DELAY) {

	        startedRunningAt = System.currentTimeMillis();

	        if (startedRunningAt > 0) {

		        Lawg.i("Bitcoin Service Starting...");

		        LocalBroadcastManager.getInstance(this).registerReceiver(localBroadcastReceiver, new IntentFilter(ACTION_DNS_SEED_DISCOVERY_COMPLETE));
		        LocalBroadcastManager.getInstance(this).registerReceiver(localBroadcastReceiver, new IntentFilter(ACTION_SERVICE_STARTED));
		        LocalBroadcastManager.getInstance(this).registerReceiver(localBroadcastReceiver, new IntentFilter(ACTION_SERVICE_DESTROYED));
		        LocalBroadcastManager.getInstance(this).registerReceiver(localBroadcastReceiver, new IntentFilter(PeerBroadcaster.ACTION_PEER_CONNECTED));
		        LocalBroadcastManager.getInstance(this).registerReceiver(localBroadcastReceiver, new IntentFilter(PeerBroadcaster.ACTION_PEER_DISCONNECTED));

		        findPeersAndConnect();
	        } else {

		        if (getConnectedPeers().size() < MAX_CONNECTIONS) {
			        findPeersAndConnect();
		        }
	        }

	        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(PeerManagementService.ACTION_SERVICE_STARTED));
        }

        return START_STICKY;
    }

    /**
     * This will find peers to connect to either through the peer pool or by getting peers
     * using the DNS discovery
     */
    private void findPeersAndConnect() {

        peerPool = Peer.getPeerPool();

        removeConnectedPeersFromPool();

        if (peerPool.size() == 0) {
            startDnsSeedDiscovery();
        } else {
            disconnectFromPeers();
            connectToNextPeer();
        }
    }

    /**
     * Removes all peers that are connected from the peer pool
     *
     */
    private void removeConnectedPeersFromPool() {

        List<Peer> connectedPeers = getConnectedPeers();

        for (int i = 0; i < connectedPeers.size(); i++) {

            for (int j = 0; j < peerPool.size(); j++) {
                if (peerPool.get(j).address.equals(connectedPeers.get(i).address)) {
                    peerPool.remove(j);
                    break;
                }
            }
        }
    }

    /**
     * Often if the app is closed or crashes, the peers get saved in a connected state.
     * Just to be sure when the app starts we make all the peers disconnected.
     */
    private void disconnectFromPeers() {

        for (Peer peer : peerPool) {
            if (peer.connected) {
                peer.connected = false;
                peer.save();
            }
        }
    }

    /**
     * Starts the look up process to find initial peers or seeds to connect to.
     * We only want one of these running at a time.
     */
    private void startDnsSeedDiscovery() {

        if (dnsSeedDiscoveryThread == null || !dnsSeedDiscoveryThread.isRunning()) {
            dnsSeedDiscoveryThread = new DnsSeedDiscoveryThread(this);
            dnsSeedDiscoveryThread.start();
        }
    }

    /**
     * Attepts to connect to a peer in the peer pool list
     */
    private void connectToNextPeer() {

        if (getConnectedPeers().size() < MAX_CONNECTIONS) {

            Lawg.i("connectingToPeers");
            Peer peerToConnectTo = findPeerToConnectTo();

            if (peerToConnectTo != null) {

                PeerCommunicatorThread thread = new PeerCommunicatorThread(this, peerToConnectTo);
                peerCommunicatorThreads.add(thread);
                thread.start();

            } else {

                Lawg.e("No peers to connect to...");
                startDnsSeedDiscovery();
            }

        } else {

            Lawg.e("No peers to connect to...");
            startDnsSeedDiscovery();
        }
    }

    /**
     * Gets a list of connected peers.
     * */
    public List<Peer> getConnectedPeers() {

        List<Peer> peers = new ArrayList<>();

        for (int i = 0; i < peerCommunicatorThreads.size(); i++) {
            PeerCommunicatorThread thread = peerCommunicatorThreads.get(i);
            if (thread.isSocketConnected() && thread.getPeer() != null) {
                peers.add(thread.getPeer());
            }
        }
        return peers;
    }

	/**
	 * Called when a peer is connected or disconnected and needs to be removed from the peer pool
	 *
	 * @param peer
	 */
	private void removePeerFromPool(Peer peer) {

	    for (Peer storedPeer : peerPool) {

		    if (storedPeer.address.equals(peer.address)) {

			    Lawg.i("remove peer: " + peer.address);
			    peerPool.remove(storedPeer);

			    break;
		    }
	    }
    }

	/**
	 * Removes a PeerCommunicatorThread from the peerCommunicatorThreads array.
	 * Usually called when a peer disconnects and is removed from the DB but
	 * hasn't yet been removed from the thread array or the peerPool
	 * @param peer
	 */
	private void removeCommunicatorThreadFromArray(Peer peer) {

	    for (PeerCommunicatorThread thread : peerCommunicatorThreads) {

		    if (thread.getPeer().address.equals(peer.address)) {

			    Lawg.i("remove thread: " + thread.getPeer().address);
			    peerCommunicatorThreads.remove(thread);

			    break;
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

        for (int i = 0; i < peerPool.size(); i++) {

            Peer peer  = peerPool.get(i);

            if (!peer.connected) {

                return peer;
            }
        }

        return null;
    }

    private void killPeerCommunicatorThreads() {

        for (int i = 0; i < peerCommunicatorThreads.size(); i++) {

            peerCommunicatorThreads.get(i).setStayConnected(false);
            peerCommunicatorThreads.get(i).interrupt();
        }
    }

    @Override
    public void onDestroy() {
        Lawg.i("onDestroy");

        Intent dnsSeedDiscoveryCompleteIntent = new Intent(PeerManagementService.ACTION_SERVICE_DESTROYED);
        LocalBroadcastManager.getInstance(this).sendBroadcast(dnsSeedDiscoveryCompleteIntent);

        disconnectFromPeers();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(localBroadcastReceiver);

        if (dnsSeedDiscoveryThread != null) {

            dnsSeedDiscoveryThread.interrupt();
        }

        new PeerBroadcaster(this, new Peer(App.monitoringPeerIP)).broadcastLogAll("Bitcoin Service Shutting down...", ChatLog.TYPE_NEUTRAL);

        killPeerCommunicatorThreads();

        stopSelf();
	    startedRunningAt = 0;

        super.onDestroy();
    }

    /**
     * Clients can call this to see if the service is running or not.
     * @return true if yes, false if not.
     */
    public boolean isRunning() {
        return startedRunningAt > 0;
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
            	peerPool = Peer.getPeerPool();
                connectToNextPeer();
            }

            if (intent.getAction().equalsIgnoreCase(PeerBroadcaster.ACTION_PEER_CONNECTED)) {

                Peer connectedPeer = intent.getParcelableExtra(PeerBroadcaster.KEY_PEER);
	            Lawg.i("Peer connected: " + connectedPeer.address);

                removePeerFromPool(connectedPeer);
                connectToNextPeer();
            }

            if (intent.getAction().equalsIgnoreCase(PeerBroadcaster.ACTION_PEER_DISCONNECTED)) {

                Peer disconnectedPeer = intent.getParcelableExtra(PeerBroadcaster.KEY_PEER);
	            Lawg.i("Peer disconnected: " + disconnectedPeer.address);

                removeCommunicatorThreadFromArray(disconnectedPeer);
	            removePeerFromPool(disconnectedPeer);
                connectToNextPeer();
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
