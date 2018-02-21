package com.boetchain.bitcoinnode.ui.adapter;

import android.content.Context;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.boetchain.bitcoinnode.App;
import com.boetchain.bitcoinnode.R;
import com.boetchain.bitcoinnode.model.Peer;
import com.boetchain.bitcoinnode.util.Lawg;
import com.squareup.picasso.Picasso;

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

            holder.listitem_peer_profile_iv = view.findViewById(R.id.listitem_peer_profile_iv);
            holder.ipTextView = view.findViewById(R.id.listitem_peer_ip_tv);
            holder.timestampTextView = view.findViewById(R.id.listitem_peer_timestamp_tv);
            holder.listitem_peer_status_tv = view.findViewById(R.id.listitem_peer_status_tv);

        } else {

            holder = (ViewHolder) view.getTag();
        }

        holder.ipTextView.setText(peer.address);
        int gmtOffSet = 0;
        holder.timestampTextView.setText(DateUtils.getRelativeTimeSpanString(peer.timestamp + gmtOffSet, System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS, 0));
        setPeerProfile(holder, peer);
        setPeerStatus(holder, peer);

        return view;
    }

    /**
     * Sets the peers profile pic to a country flag, if we have geolocated the peer.
     * @param holder - view holder.
     * @param peer - peer to display a profile pic of.
     */
    private void setPeerProfile(ViewHolder holder, Peer peer) {
        if (peer.countryCode != null && !peer.countryCode.isEmpty()) {
            Log.i(App.TAG, "http://www.countryflags.io/" + peer.countryCode + "/flat/64.png");


            int resId =
		            context.getResources()
                               .getIdentifier(peer.countryCode.toLowerCase(),
                                              "drawable", context.getPackageName());
	        Lawg.i("res name: " + "R.drawable." + peer.countryCode.toLowerCase() + " - " + resId);
            if (resId != 0) {

	            Picasso.with(context).load(resId)
	                   .error(R.mipmap.profile)
	                   .placeholder(R.mipmap.profile)
	                   .into(holder.listitem_peer_profile_iv);

            } else {

	            Picasso.with(context).load("http://www.countryflags.io/" + peer.countryCode + "/flat/64.png")
	                   .error(R.mipmap.profile)
	                   .placeholder(R.mipmap.profile)
	                   .into(holder.listitem_peer_profile_iv);
            }
        }
    }

    /**
     * Displays the status of the peer (a little bit of text under the ip).
     * Lets the user know whats happening with the peer.
     * @param holder - view holder.
     * @param peer - peer to display a status of.
     */
    private void setPeerStatus(ViewHolder holder, Peer peer) {
        if (!peer.country.isEmpty() && !peer.city.isEmpty()) {
            String status = context.getString(R.string.listitem_peer_status);
            status = status.replace("{:city}", peer.city);
            status = status.replace("{:country}", peer.country);
            holder.listitem_peer_status_tv.setText(status);
            holder.listitem_peer_status_tv.setVisibility(View.VISIBLE);
        } else {
            holder.listitem_peer_status_tv.setVisibility(View.GONE);
        }
    }

    public static class ViewHolder {

        public ImageView listitem_peer_profile_iv;
        public TextView ipTextView;
        public TextView timestampTextView;
        public TextView listitem_peer_status_tv;
    }
}
