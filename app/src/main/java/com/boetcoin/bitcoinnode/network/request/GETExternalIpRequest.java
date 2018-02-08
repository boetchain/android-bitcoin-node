package com.boetcoin.bitcoinnode.network.request;

import android.content.Context;

import com.android.volley.Response;
import com.boetcoin.bitcoinnode.network.response.GETExternalIpResponse;

/**
 * Created by rossbadenhorst on 2018/02/08.
 */

public class GETExternalIpRequest extends BaseRequest<GETExternalIpResponse> {

    public GETExternalIpRequest(Context context, Response.Listener<GETExternalIpResponse> listener, Response.ErrorListener errorListener) {
        super(context, Method.GET, getFullUrl(), GETExternalIpResponse.class, listener, errorListener);
    }

    private static String getFullUrl() {
        return "http://ip.jsontest.com/";
    }
}
