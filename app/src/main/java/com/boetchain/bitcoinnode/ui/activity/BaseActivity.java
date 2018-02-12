package com.boetchain.bitcoinnode.ui.activity;

import android.support.v7.app.AppCompatActivity;

import com.boetchain.bitcoinnode.App;

/**
 * Created by Tyler Hogarth on 2018/02/10.
 */

public class BaseActivity extends AppCompatActivity {

    @Override
    protected void onPause() {
        super.onPause();
        App.isOpen = false;
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        App.isOpen = true;
    }
}
