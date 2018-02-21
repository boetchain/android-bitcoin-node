package com.boetchain.bitcoinnode.ui.view;

import android.view.MotionEvent;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;

import com.boetchain.bitcoinnode.R;
import com.boetchain.bitcoinnode.ui.activity.MainActivity;

/**
 * Created by Ross Badenhorst.
 */
public class DrawerHeaderView {

    private TextView drawer_header_view_status_tv;
    private TextView drawer_header_view_connected_peers_tv;
    private Switch drawer_header_view_switch;

    private OnServiceChangeListener serviceChangeListener;

    /**
     * True if the VPN is being turned on and we want to prevent the user from changing the
     * state of the switch button.
     */
    private boolean switching = false;

    private MainActivity mainActivity;
    private View view;

    public DrawerHeaderView(final MainActivity mainActivity, boolean status, int connectedPeers) {
        this.mainActivity = mainActivity;

        view = View.inflate(mainActivity, R.layout.drawer_header_view, null);

        drawer_header_view_status_tv = view.findViewById(R.id.drawer_header_view_status_tv);
        drawer_header_view_connected_peers_tv = view.findViewById(R.id.drawer_header_view_connected_peers_tv);
        drawer_header_view_switch = view.findViewById(R.id.drawer_header_view_switch);

        setStatus(status);
        setConnectedPeers(connectedPeers);

        drawer_header_view_switch.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP && !switching) {

                    onSwitchTouch(((Switch) v));
                }
                return true;
            }


        });
    }

    public void setStatus(boolean on) {

        String status;

        if (on) {
            status = mainActivity.getString(R.string.drawer_header_status_on);
            drawer_header_view_connected_peers_tv.setVisibility(View.VISIBLE);
        } else {
            status = mainActivity.getString(R.string.drawer_header_status_off);
            drawer_header_view_connected_peers_tv.setVisibility(View.GONE);
        }

        String str = mainActivity.getString(R.string.drawer_header_status);

        drawer_header_view_status_tv.setText(str.replace("{:value}", status));
        drawer_header_view_switch.setChecked(on);
    }

    public void setConnectedPeers(int connectedPeers) {

    String str = mainActivity.getString(R.string.drawer_header_connected_peers);

        drawer_header_view_connected_peers_tv.setText(str.replace("{:value}", connectedPeers + ""));
    }

    private void onSwitchTouch(Switch sw) {

        sw.setChecked(!sw.isChecked());
        setStatus(sw.isChecked());

        if (serviceChangeListener != null) {
            serviceChangeListener.onServiceChange(sw.isChecked());
        }
    }

    public View getView() {
        return view;
    }

    public void setSwitching(boolean switching) {
        this.switching = switching;
    }

    public void setOnServiceChangeListener(OnServiceChangeListener serviceChangeListener) {
        this.serviceChangeListener = serviceChangeListener;
    }

    public interface OnServiceChangeListener {

        void onServiceChange(boolean on);
    }
}
