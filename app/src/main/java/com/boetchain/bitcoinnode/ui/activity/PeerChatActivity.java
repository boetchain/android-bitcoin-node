package com.boetchain.bitcoinnode.ui.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import com.boetchain.bitcoinnode.App;
import com.boetchain.bitcoinnode.R;
import com.boetchain.bitcoinnode.model.ChatLog;
import com.boetchain.bitcoinnode.model.Peer;
import com.boetchain.bitcoinnode.ui.adapter.ChatLogAdapter;
import com.boetchain.bitcoinnode.util.Notify;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Tyler Hogarth on 2018/02/10.
 */

public class PeerChatActivity extends BaseActivity {

    public static final String EXTRA_MSG = MainActivity.class.getSimpleName() + ".EXTRA_MSG";
    public static final String EXTRA_TYPE = MainActivity.class.getSimpleName() + ".EXTRA_TYPE";
    public static final String EXTRA_PEER = PeerChatActivity.class.getSimpleName() + ".EXTRA_PEER";

    private Peer peer;

    private ListView listView;
    private ChatLogAdapter adapter;
    private List<ChatLog> logs;

    private BroadcastReceiver logReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.hasExtra(EXTRA_MSG)) {

                int type = intent.getIntExtra(EXTRA_TYPE, ChatLog.TYPE_NEUTRAL);
                String msg = intent.getStringExtra(EXTRA_MSG);
                PeerChatActivity.this.logToUI(new ChatLog(msg, type));
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_peer_chat);

        if (!getIntent().hasExtra(EXTRA_PEER)) {
            Notify.toast(this, R.string.error_peer_not_found, Toast.LENGTH_SHORT);
            finish();
        } else {

            peer = getIntent().getParcelableExtra(EXTRA_PEER);

            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setTitle(peer.address);
            }

            listView = (ListView) findViewById(R.id.activity_main_log_lv);
            logs = new ArrayList();
            adapter = new ChatLogAdapter(this, logs);
            listView.setAdapter(adapter);
        }

        for (int i = 0; i < 20; i++) {
            logs.add(new ChatLog("test", new Random().nextInt(3)));
        }
    }

    private void logToUI(ChatLog log) {
        logs.add(log);
        adapter.notifyDataSetChanged();
        listView.setSelection(adapter.getCount() - 1);
    }

    @Override
    protected void onPause() {
        super.onPause();
        App.monitoringPeerIP = "";
        unregisterReceiver(logReceiver);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();

        App.monitoringPeerIP = peer.address;

        IntentFilter intent = new IntentFilter(getBroadcastAction());
        registerReceiver(logReceiver, intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * The broadcast action for receiving logs changes based on the current App.monitoringPeerIp
     *
     * @return
     */
    public static String getBroadcastAction() {
        return MainActivity.class.getSimpleName() + ".ACTION_LOG_TO_UI." + App.monitoringPeerIP;
    }
}
