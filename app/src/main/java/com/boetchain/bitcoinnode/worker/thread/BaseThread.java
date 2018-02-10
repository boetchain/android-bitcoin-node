package com.boetchain.bitcoinnode.worker.thread;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Created by rossbadenhorst on 2018/02/08.
 */

public abstract class BaseThread implements Runnable {

    protected RequestQueue requestQueue;
    protected Context context;

    public BaseThread(Context context) {
        this.context = context;
        requestQueue = Volley.newRequestQueue(context);
    }
}
