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
import android.widget.ListView;
import android.widget.Switch;
import android.widget.Toast;

import com.boetchain.bitcoinnode.R;
import com.boetchain.bitcoinnode.model.Peer;
import com.boetchain.bitcoinnode.ui.adapter.PeerAdapter;
import com.boetchain.bitcoinnode.util.Lawg;
import com.boetchain.bitcoinnode.worker.service.PeerManagementService;

import java.util.List;

public class MainActivity extends BaseActivity implements AdapterView.OnItemClickListener {

    private ListView listView;

    private PeerAdapter adapter;
    private List<Peer> peers;

    private Switch peerMgmtSwitch;

    private PeerManagementService peerManagementService;

    private BroadcastReceiver peerReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            Lawg.d("REFREEESH Peer list");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listView = (ListView) findViewById(R.id.activity_main_log_lv);
        peers = Peer.getConnectedPeers();
        adapter = new PeerAdapter(this, peers);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        LocalBroadcastManager.getInstance(this).unregisterReceiver(peerReceiver);

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
        filter.addAction(PeerManagementService.ACTION_PEER_CONNECTED);
        filter.addAction(PeerManagementService.ACTION_PEER_DISCONNECTED);
        LocalBroadcastManager.getInstance(this).registerReceiver(peerReceiver, filter);

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
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            setServiceState();
        }
    };
}
