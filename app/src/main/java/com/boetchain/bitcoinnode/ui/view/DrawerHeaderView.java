package com.boetchain.bitcoinnode.ui.view;

import android.content.Context;
import android.view.View;

import com.boetchain.bitcoinnode.R;

/**
 * Created by Ross Badenhorst.
 */
public class DrawerHeaderView {

    private Context context;
    private View view;

    public DrawerHeaderView(Context context) {
        this.context = context;

        view = View.inflate(context, R.layout.drawer_header_view, null);
    }

    public View getView() {
        return view;
    }
}
