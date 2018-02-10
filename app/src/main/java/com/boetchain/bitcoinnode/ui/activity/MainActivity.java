package com.boetchain.bitcoinnode.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.boetchain.bitcoinnode.R;
import com.boetchain.bitcoinnode.model.Peer;
import com.boetchain.bitcoinnode.ui.adapter.PeerAdapter;
import com.boetchain.bitcoinnode.util.Notify;
import com.boetchain.bitcoinnode.worker.service.BitcoinService;

import java.util.List;

public class MainActivity extends BaseActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

    private boolean isTuningHowzit = false;

    private Button howzitBtn;
    private ListView listView;
    private PeerAdapter adapter;
    private List<Peer> peers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        howzitBtn = (Button) findViewById(R.id.activity_main_howzit_btn);
        howzitBtn.setOnClickListener(this);

        listView = (ListView) findViewById(R.id.activity_main_log_lv);
        peers = Peer.getConnectedPeers();
        adapter = new PeerAdapter(this, peers);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);

    }

    private void tuneHowzit() {

        toggleHowzitBtnState(true);
        Intent bitcoinService = new Intent(this, BitcoinService.class);
        startService(bitcoinService);
    }

    /**
     * Sets the howzit button to working/not working
     *
     * @param startTuning determines whether to set the state as working or not working
     */
    private void toggleHowzitBtnState(boolean startTuning) {

        if (startTuning) {

            howzitBtn.setText(getResources().getString(R.string.activity_main_howzit_btn_start_working));
        } else {

            howzitBtn.setText(getResources().getString(R.string.activity_main_howzit_btn));
        }
        isTuningHowzit = startTuning;
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.activity_main_howzit_btn:
                if (!isTuningHowzit) {
                    howzitBtn.setText(getString(R.string.activity_main_howzit_btn_start_working));
                    tuneHowzit();
                } else {
                    Notify.toast(this, R.string.activity_main_howzit_btn_toast_busy, Toast.LENGTH_SHORT);
                }
            break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

        Intent intent = new Intent(this, PeerChatActivity.class);
        intent.putExtra(PeerChatActivity.EXTRA_PEER, peers.get(i));
        startActivity(intent);
    }
}
