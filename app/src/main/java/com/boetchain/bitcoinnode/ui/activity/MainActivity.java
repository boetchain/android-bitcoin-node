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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import com.boetchain.bitcoinnode.R;
import com.boetchain.bitcoinnode.model.Peer;
import com.boetchain.bitcoinnode.ui.adapter.PeerAdapter;
import com.boetchain.bitcoinnode.util.Lawg;
import com.boetchain.bitcoinnode.worker.service.PeerManagementService;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity implements AdapterView.OnItemClickListener {

    /**
     * Switch to turn off or on the bitcoin service
     */
    private Switch peerMgmtSwitch;
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
     * Displays the status of the service during startup.
     */
    private TextView activity_main_status_tv;
    /**
     * The underlying service that handles connections with peers
     */
    private PeerManagementService peerManagementService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        activity_main_log_lv = findViewById(R.id.activity_main_log_lv);
        activity_main_status_tv = findViewById(R.id.activity_main_status_tv);
        activity_main_logo_iv = findViewById(R.id.activity_main_logo_iv);

        adapter = new PeerAdapter(this, peers);
        activity_main_log_lv.setAdapter(adapter);

        activity_main_log_lv.setOnItemClickListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        LocalBroadcastManager.getInstance(this).unregisterReceiver(localBroadcastReceiver);

        unbindService(serviceConnection);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);

        MenuItem item = menu.findItem(R.id.switchId).setActionView(R.layout.view_switch);
        peerMgmtSwitch = item.getActionView().findViewById(R.id.switchAB);

        peerMgmtSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Intent serviceIntent = new Intent(MainActivity.this, PeerManagementService.class);

                if (isChecked) {
                    MainActivity.this.startService(serviceIntent);
                } else {
                    MainActivity.this.stopService(serviceIntent);
                }
            }
        });

        setServiceState();
        return true;
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();

        IntentFilter filter = new IntentFilter();
        filter.addAction(PeerManagementService.ACTION_DNS_SEED_DISCOVERY_STARTING);
        filter.addAction(PeerManagementService.ACTION_DNS_SEED_DISCOVERY_COMPLETE);
        filter.addAction(PeerManagementService.ACTION_PEER_CONNECTION_ATTEMPT);
        filter.addAction(PeerManagementService.ACTION_PEER_CONNECTED);
        filter.addAction(PeerManagementService.ACTION_PEER_DISCONNECTED);
        LocalBroadcastManager.getInstance(this).registerReceiver(localBroadcastReceiver, filter);

        Intent intent = new Intent(this, PeerManagementService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

        Intent intent = new Intent(this, PeerChatActivity.class);
        intent.putExtra(PeerChatActivity.EXTRA_PEER, peers.get(i));
        startActivity(intent);
    }

    private void setServiceState() {
        if (peerMgmtSwitch != null && peerManagementService != null) {
            peerMgmtSwitch.setChecked(peerManagementService.isRunning());
        }
    }

    private void refreshPeers(List<Peer> updatePeers) {
        if (updatePeers.size() > 0) {
            activity_main_log_lv.setVisibility(View.VISIBLE);

            activity_main_status_tv.setVisibility(View.GONE);
            activity_main_logo_iv.setVisibility(View.GONE);
        }

        peers.clear();
        peers.addAll(updatePeers);
        adapter.notifyDataSetChanged();
    }

    private void setStatusUpdate(String newStatus) {
        Lawg.i("setStatusUpdate: " + newStatus);

        if (peers.size() > 0) {
            activity_main_log_lv.setVisibility(View.VISIBLE);

            activity_main_status_tv.setVisibility(View.GONE);
            activity_main_logo_iv.setVisibility(View.GONE);
        }


        activity_main_log_lv.setVisibility(View.GONE);
        activity_main_logo_iv.setVisibility(View.VISIBLE);
        activity_main_status_tv.setVisibility(View.VISIBLE);
        activity_main_status_tv.setText(newStatus);
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

            setServiceState();
            refreshPeers(peerManagementService.getConnectedPeers());
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            setServiceState();
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
                setStatusUpdate("Finding seed peers to connect to...");
            }

            if (intentAction.equalsIgnoreCase(PeerManagementService.ACTION_DNS_SEED_DISCOVERY_COMPLETE)) {
                setStatusUpdate("Found seed peers, attempting to connect");
            }

            if (intentAction.equalsIgnoreCase(PeerManagementService.ACTION_PEER_CONNECTION_ATTEMPT)) {
                setStatusUpdate("Trying to connect to a peer");
            }

            if (intentAction.equalsIgnoreCase(PeerManagementService.ACTION_PEER_CONNECTED)) {
                refreshPeers(peerManagementService.getConnectedPeers());
            }

            if (intentAction.equalsIgnoreCase(PeerManagementService.ACTION_PEER_DISCONNECTED)) {
                refreshPeers(peerManagementService.getConnectedPeers());
            }
        }
    };
}
