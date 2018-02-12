package com.boetchain.bitcoinnode.ui.activity;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.boetchain.bitcoinnode.R;

/**
 * Created by Ross Badenhorst.
 */
public class AboutActivity extends AppCompatActivity {

    private TextView activity_about_version_tv;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        activity_about_version_tv = findViewById(R.id.activity_about_version_tv);
        try {
            activity_about_version_tv.setText(activity_about_version_tv.getText().toString().replace("{:version}", this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName));
        } catch (PackageManager.NameNotFoundException e) {
            activity_about_version_tv.setVisibility(View.INVISIBLE);
        }
    }
}
