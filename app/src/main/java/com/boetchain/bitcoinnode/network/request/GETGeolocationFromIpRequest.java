package com.boetchain.bitcoinnode.network.request;

import android.content.Context;

import com.android.volley.Response;
import com.boetchain.bitcoinnode.model.Peer;
import com.boetchain.bitcoinnode.network.response.GETGeolocationFromIpResponse;

/**
 * Created by Ross Badenhorst.
 */
public class GETGeolocationFromIpRequest extends BaseRequest<GETGeolocationFromIpResponse> {

    /**
     * ip-api.com's key for success.
     * Set in their json response.
     */
    public static final String STATUS_SUCCESS = "success";

    /**
     * The peer we want to geolocate.
     */
    private Peer peer;

    public GETGeolocationFromIpRequest(Context context, Peer peer, Response.Listener<GETGeolocationFromIpResponse> listener, Response.ErrorListener errorListener) {
        super(context, Method.GET, getFullUrl(peer), GETGeolocationFromIpResponse.class, listener, errorListener);
        this.peer = peer;
    }

    private static String getFullUrl(Peer peer) {
        return "http://ip-api.com/json/" + peer.address;
    }
}
