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
     * The peer we want to geolocate.
     */
    private Peer peer;

    public GETGeolocationFromIpRequest(Context context, Peer peer, Response.Listener<GETGeolocationFromIpResponse> listener, Response.ErrorListener errorListener) {
        super(context, Method.GET, getFullUrl(), GETGeolocationFromIpResponse.class, listener, errorListener);
        this.peer = peer;
    }

    private static String getFullUrl() {
        return "http://ip-api.com/json/203.173.185.138";
    }
}
