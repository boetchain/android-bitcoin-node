package com.boetchain.bitcoinnode.ui.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.view.MenuItem;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.Toast;

import com.boetchain.bitcoinnode.R;
import com.boetchain.bitcoinnode.model.ChatLog;
import com.boetchain.bitcoinnode.model.Peer;
import com.boetchain.bitcoinnode.ui.adapter.ChatLogAdapter;
import com.boetchain.bitcoinnode.util.Notify;
import com.boetchain.bitcoinnode.worker.broadcaster.PeerBroadcaster;

import java.util.List;

/**
 * Created by Tyler Hogarth on 2018/02/10.
 */

public class PeerChatActivity extends BaseActivity {

    /**
     * The peer we are chatting to.
     */
    private Peer peer;

    /**
     * List of chat messages between us and the peer.
     * The most recent message should appear at the bottom.
     */
    private ListView listView;
    private ChatLogAdapter adapter;
    private List<ChatLog> logs;

    /**
     * True if the user has scrolled to the bottom of the listview
     */
    private boolean atBottom = true;

    /**
     * Listens for broadcasts from other parts of the app.
     */
    private BroadcastReceiver localBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String intentAction = intent.getAction();

            if (intentAction.equalsIgnoreCase(PeerBroadcaster.ACTION_PEER_UPDATED)) {

                /**
                 * We get updates about other peers, but since we are in this peers
                 * chat, we only want to update message for the peer we are looking at.
                 */
                Peer updatedPeer = intent.getParcelableExtra(PeerBroadcaster.KEY_PEER);
                if (peer.equals(intent.getParcelableExtra(PeerBroadcaster.KEY_PEER))) {
                    refershChat(updatedPeer.getChatHistory());
                }
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_peer_chat);

        if (!getIntent().hasExtra(PeerBroadcaster.KEY_PEER)) {
            Notify.toast(this, R.string.error_peer_not_found, Toast.LENGTH_SHORT);
            finish();
        } else {

            peer = getIntent().getParcelableExtra(PeerBroadcaster.KEY_PEER);

            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setTitle(peer.address);
            }

            listView = findViewById(R.id.activity_main_log_lv);
            listView.setOnScrollListener(new AbsListView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(AbsListView absListView, int i) {}

                @Override
                public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

                	//If the user has scrolled to the bottom of the screen OR
	                //If the number of visible items is the same as the total number of items
                    if (firstVisibleItem + visibleItemCount == totalItemCount ||
                        visibleItemCount == totalItemCount) {
                        atBottom = true;
                    } else {
                        atBottom = false;
                    }
                }
            });
            logs = peer.getChatHistory();
            adapter = new ChatLogAdapter(this, logs);
            listView.setAdapter(adapter);
        }
    }

    /**
     * Updates the chat with a new list of messages.
     * As the peers are blasting our faces with message, this list
     * should be trimmed to MAX_CHAT_HISTORY_SIZE
     * @param updatedChat - new messages from the peer.
     */
    private void refershChat(List<ChatLog> updatedChat) {
        logs.clear();
        logs.addAll(updatedChat);
        adapter.notifyDataSetChanged();

	    scrollToBottom();
    }

    private void scrollToBottom() {
	    if (atBottom) {
		    listView.setSelection(adapter.getCount() - 1);
	    }
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(localBroadcastReceiver);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(localBroadcastReceiver, new IntentFilter(PeerBroadcaster.ACTION_PEER_UPDATED));

	    scrollToBottom();
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
}
