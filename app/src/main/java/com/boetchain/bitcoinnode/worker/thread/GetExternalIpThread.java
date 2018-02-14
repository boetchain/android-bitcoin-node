package com.boetchain.bitcoinnode.worker.thread;

import android.content.Context;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.boetchain.bitcoinnode.network.request.GETExternalIpRequest;
import com.boetchain.bitcoinnode.network.response.GETExternalIpResponse;
import com.boetchain.bitcoinnode.util.Prefs;

/**
 * Created by rossbadenhorst on 2018/02/08.
 */

public class GetExternalIpThread extends BaseRunnable implements Response.Listener<GETExternalIpResponse>, Response.ErrorListener  {

    public GetExternalIpThread(Context context) {
        super(context);
    }

    @Override
    public void run() {
        GETExternalIpRequest getExternalIpRequest = new GETExternalIpRequest(context, this, this);
        requestQueue.add(getExternalIpRequest);
    }

    @Override
    public void onErrorResponse(VolleyError error) {

    }

    @Override
    public void onResponse(GETExternalIpResponse response) {
        Prefs.put(context, Prefs.KEY_EXTERNAL_IP, response.ip);
    }
}
