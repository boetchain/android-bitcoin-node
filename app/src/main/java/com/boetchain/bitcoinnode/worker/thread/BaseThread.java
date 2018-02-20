package com.boetchain.bitcoinnode.worker.thread;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Created by rossbadenhorst on 2018/02/08.
 */

public abstract class BaseThread extends Thread {

    protected RequestQueue requestQueue;
    protected Context context;
    private boolean isRunning = false;

    public BaseThread(Context context) {
        this.context = context;
        requestQueue = Volley.newRequestQueue(context);
    }

    @Override
    public final void run() {
        isRunning = true;
        super.run();
        runThread();
        isRunning = false;
    }

    public boolean isRunning() {

        if (isInterrupted()) {
            isRunning = false;
        }
        return isRunning;
    }

    public abstract void runThread();
}
