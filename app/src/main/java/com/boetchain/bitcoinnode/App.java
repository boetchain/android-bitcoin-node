package com.boetchain.bitcoinnode;

import android.content.Context;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.Volley;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Created by rossbadenhorst on 2018/01/31.
 */

public class App extends com.orm.SugarApp {
    public static final String TAG = "bitcoinnode";

    /**
     * True if the app is currently open and in the foreground.
     * Set true/false in the onPostResume/onPause methods in
     * BaseActivity
     */
    public static boolean isOpen = false;

    /**
     * Used by volley, the networking dependency, to fire requests in a queue.
     */
    private static RequestQueue requestQueue;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TAG, "Starting...");
    }

    /**
     * Gets the request queue for volley.
     * @return
     */
    public static RequestQueue getRequestQueue(Context context) {
        if (requestQueue == null) {
            HurlStack stack = new HurlStack(null, createSslSocketFactory());
            requestQueue = Volley.newRequestQueue(context, stack);
        }
        return requestQueue;
    }

    /**
     * Creates a socket factory for volley.
     * Used for https requests.
     * @return - socketFactory.
     */
    private static SSLSocketFactory createSslSocketFactory() {
        TrustManager[] byPassTrustManagers = new TrustManager[]{new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }

            public void checkClientTrusted(X509Certificate[] chain, String authType) {
            }

            public void checkServerTrusted(X509Certificate[] chain, String authType) {
            }
        }};

        SSLContext sslContext = null;
        SSLSocketFactory sslSocketFactory = null;
        try {
            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, byPassTrustManagers, new SecureRandom());
            sslSocketFactory = sslContext.getSocketFactory();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }

        return sslSocketFactory;
    }
}
