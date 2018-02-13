package com.boetchain.bitcoinnode.ui.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.boetchain.bitcoinnode.R;
import com.boetchain.bitcoinnode.model.Peer;

import java.util.List;

/**
 * Created by Ross Badenhorst.
 */
public class StatusAdapter extends BaseAdapter {

    private Context context;
    private List<String> statusMessages;

    public StatusAdapter(Context context, List<String> statusMessages) {
        this.context = context;
        this.statusMessages = statusMessages;
    }

    @Override
    public int getCount() {
        return statusMessages.size();
    }

    @Override
    public String getItem(int i) {
        return statusMessages.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        String message = statusMessages.get(i);

        StatusAdapter.ViewHolder holder;
        if (view == null) {

            holder = new StatusAdapter.ViewHolder();
            view = View.inflate(context, R.layout.listitem_status_message, null);
            view.setTag(holder);

            holder.statusMessage = (TextView) view.findViewById(R.id.listitem_status_message_tv);

        } else {

            holder = (StatusAdapter.ViewHolder) view.getTag();
        }

        holder.statusMessage.setText(message);

        return view;
    }

    public static class ViewHolder {

        public TextView statusMessage;
    }
}
