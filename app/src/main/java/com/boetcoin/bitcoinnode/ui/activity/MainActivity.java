package com.boetcoin.bitcoinnode.ui.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.boetcoin.bitcoinnode.R;
import com.boetcoin.bitcoinnode.model.LogItem;
import com.boetcoin.bitcoinnode.ui.adapter.LogAdapter;
import com.boetcoin.bitcoinnode.util.Lawg;
import com.boetcoin.bitcoinnode.util.Notify;
import com.boetcoin.bitcoinnode.worker.service.BitcoinService;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String ACTION_LOG_TO_UI = MainActivity.class.getName() + ".ACTION_LOG_TO_UI";
    public static final String EXTRA_MSG = MainActivity.class.getName() + ".EXTRA_MSG";
    public static final String EXTRA_TYPE = MainActivity.class.getName() + ".EXTRA_TYPE";

    private boolean isTuningHowzit = false;

    private Button howzitBtn;
    private ListView listView;
    private LogAdapter adapter;
    private List<LogItem> logs;

    private BroadcastReceiver logReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.hasExtra(EXTRA_MSG)) {

                int type = intent.getIntExtra(EXTRA_TYPE, LogItem.TI);
                String msg = intent.getStringExtra(EXTRA_MSG);
                MainActivity.this.logToUI(new LogItem(type, msg));
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        howzitBtn = (Button) findViewById(R.id.activity_main_howzit_btn);
        howzitBtn.setOnClickListener(this);

        listView = (ListView) findViewById(R.id.activity_main_log_lv);
        logs = new ArrayList();
        adapter = new LogAdapter(this, logs);
        listView.setAdapter(adapter);

    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(logReceiver);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        IntentFilter intent = new IntentFilter(ACTION_LOG_TO_UI);
        registerReceiver(logReceiver, intent);
    }

    private void logToUI(LogItem log) {
        logs.add(log);
        adapter.notifyDataSetChanged();
        listView.setSelection(adapter.getCount() - 1);
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
                    Lawg.u(this, "Let's start tuning...", LogItem.TI);
                    howzitBtn.setText(getString(R.string.activity_main_howzit_btn_start_working));
                    tuneHowzit();
                } else {
                    Notify.toast(this, R.string.activity_main_howzit_btn_toast_busy, Toast.LENGTH_SHORT);
                }
            break;
        }
    }
}
