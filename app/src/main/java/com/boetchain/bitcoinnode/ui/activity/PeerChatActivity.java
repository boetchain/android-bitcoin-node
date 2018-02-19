package com.boetchain.bitcoinnode.ui.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.MenuItem;
import android.widget.AbsListView;
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

/**
 * Created by Tyler Hogarth on 2018/02/10.
 */

public class PeerChatActivity extends BaseActivity {

    public static final String EXTRA_TEXT = MainActivity.class.getSimpleName() + ".EXTRA_TEXT";
    public static final String EXTRA_COMMAND = MainActivity.class.getSimpleName() + ".EXTRA_COMMAND";
    public static final String EXTRA_TIME = MainActivity.class.getSimpleName() + ".EXTRA_TIME";
    public static final String EXTRA_TYPE = MainActivity.class.getSimpleName() + ".EXTRA_TYPE";
    public static final String EXTRA_PEER = PeerChatActivity.class.getSimpleName() + ".EXTRA_PEER";

    private Peer peer;

    private ListView listView;
    private ChatLogAdapter adapter;
    private List<ChatLog> logs;

    /**
     * True if the user has scrolled to the bottom of the listview
     */
    private boolean atBottom;

    private BroadcastReceiver logReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.hasExtra(EXTRA_TEXT)) {

                String text = intent.getStringExtra(EXTRA_TEXT);
                String command = intent.getStringExtra(EXTRA_COMMAND);
                long time = intent.getLongExtra(EXTRA_TIME, System.currentTimeMillis());
                int type = intent.getIntExtra(EXTRA_TYPE, ChatLog.TYPE_NEUTRAL);
                PeerChatActivity.this.logToUI(new ChatLog(text, command, time, type));
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
            listView.setOnScrollListener(new AbsListView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(AbsListView absListView, int i) {}

                @Override
                public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                    if (firstVisibleItem + visibleItemCount == totalItemCount) {
                        atBottom = true;
                    } else {
                        atBottom = false;
                    }
                }
            });
            logs = new ArrayList();
            adapter = new ChatLogAdapter(this, logs);
            listView.setAdapter(adapter);
        }
    }

    private void logToUI(ChatLog log) {
        logs.add(log);
        adapter.notifyDataSetChanged();

        if (atBottom) {
            listView.setSelection(adapter.getCount() - 1);
        }
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
