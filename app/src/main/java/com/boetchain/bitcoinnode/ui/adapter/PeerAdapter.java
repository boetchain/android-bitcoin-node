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
 * Created by Tyler Hogarth on 2018/02/10.
 */

public class PeerAdapter extends BaseAdapter {

    private Context context;
    private List<Peer> peers;

    public PeerAdapter(Context context, List<Peer> peers) {
        this.context = context;
        this.peers = peers;

    }

    @Override
    public int getCount() {
        return peers.size();
    }

    @Override
    public Peer getItem(int i) {
        return peers.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        Peer peer = peers.get(i);

        ViewHolder holder;
        if (view == null) {

            holder = new ViewHolder();
            view = View.inflate(context, R.layout.listitem_peer, null);
            view.setTag(holder);

            holder.ipTextView = (TextView) view.findViewById(R.id.listitem_peer_ip_tv);
            holder.timestampTextView = (TextView) view.findViewById(R.id.listitem_peer_timestamp_tv);

        } else {

            holder = (ViewHolder) view.getTag();
        }

        holder.ipTextView.setText(peer.address);
        holder.timestampTextView.setText(peer.timestamp + "");

        return view;
    }

    public static class ViewHolder {

        public TextView ipTextView;
        public TextView timestampTextView;
    }
}
