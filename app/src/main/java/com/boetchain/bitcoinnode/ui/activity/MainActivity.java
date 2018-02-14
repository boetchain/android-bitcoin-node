package com.boetchain.bitcoinnode.ui.activity;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;

import com.boetchain.bitcoinnode.R;
import com.boetchain.bitcoinnode.model.Peer;
import com.boetchain.bitcoinnode.ui.adapter.PeerAdapter;
import com.boetchain.bitcoinnode.ui.adapter.StatusAdapter;
import com.boetchain.bitcoinnode.util.Lawg;
import com.boetchain.bitcoinnode.worker.broadcaster.PeerBroadcaster;
import com.boetchain.bitcoinnode.worker.service.PeerManagementService;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

    /**
     * Gets this shit on the road...
     */
    private Button activity_main_gobaby_btn;
    /**
     * Peer list view, contains peers we are connected to.
     */
    private ListView activity_main_log_lv;
    private PeerAdapter adapter;
    /**
     * List of peers we want to display to the user.
     */
    private List<Peer> peers = new ArrayList<>();
    private ImageView activity_main_logo_iv;
    /**
     * The underlying service that handles connections with peers
     */
    private PeerManagementService peerManagementService;

    /**
     * List view displaying status messages to the user.
     */
    private ListView activity_main_status_lv;
    private StatusAdapter statusAdapter;
    /**
     * List of status messages to display to the user.
     */
    private List<String> statusMessages = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        activity_main_gobaby_btn = findViewById(R.id.activity_main_gobaby_btn);
        activity_main_log_lv = findViewById(R.id.activity_main_log_lv);
        activity_main_status_lv = findViewById(R.id.activity_main_status_lv);
        activity_main_logo_iv = findViewById(R.id.activity_main_logo_iv);

        statusAdapter = new StatusAdapter(this, statusMessages);
        activity_main_status_lv.setAdapter(statusAdapter);

        adapter = new PeerAdapter(this, peers);
        activity_main_log_lv.setAdapter(adapter);

        activity_main_gobaby_btn.setOnClickListener(this);
        activity_main_log_lv.setOnItemClickListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        LocalBroadcastManager.getInstance(this).unregisterReceiver(localBroadcastReceiver);

        unbindService(serviceConnection);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();

        IntentFilter filter = new IntentFilter();
        filter.addAction(PeerManagementService.ACTION_DNS_SEED_DISCOVERY_STARTING);
        filter.addAction(PeerManagementService.ACTION_DNS_SEED_DISCOVERY_COMPLETE);
        filter.addAction(PeerBroadcaster.ACTION_PEER_CONNECTION_ATTEMPT);
        filter.addAction(PeerBroadcaster.ACTION_PEER_CONNECTED);
        filter.addAction(PeerBroadcaster.ACTION_PEER_DISCONNECTED);
        LocalBroadcastManager.getInstance(this).registerReceiver(localBroadcastReceiver, filter);

        Intent intent = new Intent(this, PeerManagementService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.activity_main_gobaby_btn:
                Intent serviceIntent = new Intent(MainActivity.this, PeerManagementService.class);
                MainActivity.this.startService(serviceIntent);

                Animation startRotateAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate);
                activity_main_logo_iv.startAnimation(startRotateAnimation);
                activity_main_gobaby_btn.setVisibility(View.GONE);
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

        Intent intent = new Intent(this, PeerChatActivity.class);
        intent.putExtra(PeerChatActivity.EXTRA_PEER, peers.get(i));
        startActivity(intent);
    }

    /**
     * Updates the peers in the list view.
     * If there are peers to display, we hide other elements on the screen
     * such as the logo etc.
     * @param updatePeers - list of peers to show the user.
     */
    private void refreshPeers(List<Peer> updatePeers) {
        if (updatePeers.size() > 0) {
            activity_main_log_lv.setVisibility(View.VISIBLE);

            activity_main_status_lv.setVisibility(View.GONE);
            activity_main_logo_iv.setVisibility(View.GONE);
        }

        peers.clear();
        peers.addAll(updatePeers);
        adapter.notifyDataSetChanged();
    }

    /**
     * Adds a status to let the user know what is going on.
     * If there are peers, we rather show the user that
     * then some stupid status message no one reads anyways...
     * @param newStatus - to show the user.
     */
    private void setStatusUpdate(String newStatus) {
        if (peers.size() > 0) {
            activity_main_log_lv.setVisibility(View.VISIBLE);

            activity_main_status_lv.setVisibility(View.GONE);
            activity_main_logo_iv.setVisibility(View.GONE);
            activity_main_gobaby_btn.setVisibility(View.GONE);
        }


        activity_main_log_lv.setVisibility(View.GONE);
        activity_main_logo_iv.setVisibility(View.VISIBLE);
        activity_main_status_lv.setVisibility(View.VISIBLE);

        statusMessages.add(0 , newStatus);
        statusAdapter.notifyDataSetChanged();
    }

    /**
     * Allows us to make comms with the PeerManagement Service
     */
    private ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Lawg.i("onServiceConnected");
            PeerManagementService.LocalBinder binder = (PeerManagementService.LocalBinder) iBinder;
            peerManagementService = binder.getService();

            refreshPeers(peerManagementService.getConnectedPeers());
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };

    /**
     * Listens for broadcasts from other parts of the app.
     */
    private BroadcastReceiver localBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String intentAction = intent.getAction();

            if (intentAction.equalsIgnoreCase(PeerManagementService.ACTION_DNS_SEED_DISCOVERY_STARTING)) {
                setStatusUpdate(getString(R.string.activity_main_status_find_seeds_start));
            }

            if (intentAction.equalsIgnoreCase(PeerManagementService.ACTION_DNS_SEED_DISCOVERY_COMPLETE)) {
                int peersFoundFromDnsSeeds = intent.getParcelableArrayListExtra(PeerBroadcaster.KEY_PEERS).size();
                setStatusUpdate(getString(R.string.activity_main_status_find_seeds_complete).replace("{:value}", "" +peersFoundFromDnsSeeds));
            }

            if (intentAction.equalsIgnoreCase(PeerBroadcaster.ACTION_PEER_CONNECTION_ATTEMPT)) {
                String peerAddress = ((Peer)intent.getParcelableExtra(PeerBroadcaster.KEY_PEER)).address;
                setStatusUpdate(getString(R.string.activity_main_status_connect_to_peer).replace("{:value}", peerAddress));
            }

            if (intentAction.equalsIgnoreCase(PeerBroadcaster.ACTION_PEER_CONNECTED)) {
                refreshPeers(peerManagementService.getConnectedPeers());
            }

            if (intentAction.equalsIgnoreCase(PeerBroadcaster.ACTION_PEER_DISCONNECTED)) {
                refreshPeers(peerManagementService.getConnectedPeers());
            }
        }
    };
}
