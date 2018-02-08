package com.boetcoin.bitcoinnode.network.request;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.boetcoin.bitcoinnode.network.response.BaseResponse;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by rossbadenhorst on 2018/02/08.
 */

public abstract class BaseRequest<T extends BaseResponse> extends Request<T> {
    public static final String TAG = BaseRequest.class.getSimpleName();


    public static final int   RETRY_TIMEOUT   = 30000;
    public static final int   RETRY_COUNT     = 0;
    public static final float RETRY_BACKOFF   = 1f;

    protected static final Gson gson = new Gson();
    private final Class<T> clazz;
    private final Response.Listener<T> listener;
    protected final Context context;

    /**
     * The headers that are part of the HTTP request.
     */
    protected Map<String, String> headers;
    /**
     * The params that are part of the HTTP request.
     */
    protected Map<String, String> params;

    public BaseRequest(Context context, int method, String url, Class<T> clazz, Response.Listener<T> listener, Response.ErrorListener errorListener) {
        super(method, url, errorListener);
        this.context    = context;
        this.clazz      = clazz;
        this.listener   = listener;

        Log.i(TAG, " - URL: " + url);

        setRetryPolicy(new DefaultRetryPolicy(RETRY_TIMEOUT,
                RETRY_COUNT,
                RETRY_BACKOFF));
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        super.getHeaders();
        headers = new HashMap<>();
        return headers;
    }

    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        super.getParams();
        params = new HashMap<>();
        return params;
    }

    @Override
    protected void deliverResponse(T response) {
        if (listener != null) {
            listener.onResponse(response);
        }
    }

    protected void onPreError(VolleyError error) {
    }

    @Override
    public void deliverError(VolleyError error) {
        printError(error);
        onPreError(error);
        super.deliverError(error);
    }

    protected void printError(VolleyError error) {
        if (error != null && error.getMessage() != null) {
            Log.e(TAG, " ERROR: " + error.getMessage());
        } else {
            Log.e(TAG, " ERROR: Request Failed!");
        }
    }

    @Override
    protected Response<T> parseNetworkResponse(NetworkResponse response) {
        try {
            String json = new String(response.data, "UTF-8");
            Log.i(TAG," JSON: " + json);
            return Response.success(gson.fromJson(json, clazz), HttpHeaderParser.parseCacheHeaders(response));
        } catch (Exception e) {
            return Response.error(new ParseError(e));
        }
    }
}
