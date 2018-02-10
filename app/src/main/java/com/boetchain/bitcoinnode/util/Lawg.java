package com.boetchain.bitcoinnode.util;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.boetchain.bitcoinnode.App;
import com.boetchain.bitcoinnode.model.LogItem;
import com.boetchain.bitcoinnode.model.Peer;
import com.boetchain.bitcoinnode.ui.activity.PeerChatActivity;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * User: Tyler
 * Date: 2014/08/21
 * Time: 05:15 PM
 */
public class Lawg {

    public static boolean DEV_MODE = true;

    static String className;
    static String methodName;
    static int lineNumber;

    public static String createLog(String log) {

        StringBuffer buffer = new StringBuffer();
        buffer.append("[");
        buffer.append(methodName);
        buffer.append(":");
        buffer.append(lineNumber);
        buffer.append("]");
        buffer.append(log);

        return buffer.toString();
    }

    public static void getMethodNames(StackTraceElement[] sElements) {
        className = sElements[1].getFileName().replace(".java", "");
        methodName = sElements[1].getMethodName() + "()";
        lineNumber = sElements[1].getLineNumber();
    }

    /**
     * Logs to i and to the UI via broadcast
     * Type determines color
     * @param context
     * @param msg
     */
    public static void u(Context context, Peer peer, String msg, int type, int log) {

        //TODO REMOVE THIS when UI is properly implemented
        switch (type) {
            case LogItem.TYPE_NEUTRAL:
                msg = "* " + msg + " *";
                break;
            case LogItem.TYPE_IN:
                msg = "<-- " + msg + " <--";
                break;
            case LogItem.TYPE_OUT:
                msg = "--> " + msg + " -->";
                break;
            default:
                msg = "? " + msg + " ?";
                break;
        }

        switch (log) {

            case LogItem.TI:
                Lawg.i(msg);
                break;

            case LogItem.TW:
                Lawg.w(msg);
                break;

            case LogItem.TD:
                Lawg.d(msg);
                break;

            case LogItem.TE:
                Lawg.e(msg);
                break;

            case LogItem.TV:
                Lawg.v(msg);
                break;

            default:
                Lawg.i(msg);
        }

        if (App.monitoringPeerIP.equals(peer.ip)) {

            Intent intent = new Intent();
            intent.setAction(PeerChatActivity.getBroadcastAction());
            intent.putExtra(PeerChatActivity.EXTRA_MSG, msg);
            intent.putExtra(PeerChatActivity.EXTRA_TYPE, type);
            intent.putExtra(PeerChatActivity.EXTRA_LOG, log);
            context.sendBroadcast(intent);
        }
    }

    public static void e(String msg) {
        if (DEV_MODE) {
            getMethodNames(new Throwable().getStackTrace());
            Log.e(className, createLog(msg));
        }
    }

    public static void w(String msg) {

        if (DEV_MODE) {
            getMethodNames(new Throwable().getStackTrace());
            Log.w(className, createLog(msg));
        }
    }

    public static void v(String msg) {
        if (DEV_MODE) {
            getMethodNames(new Throwable().getStackTrace());
            Log.v(className, createLog(msg));
        }
    }

    public static void i(String msg) {
        if (DEV_MODE) {
            getMethodNames(new Throwable().getStackTrace());
            Log.i(className, createLog(msg));
        }
    }

    public static void i(Object obj) {
        if (DEV_MODE) {
            getMethodNames(new Throwable().getStackTrace());
            Log.i(className, createLog(obj.toString()));
        }
    }

    public static void i() {
        if (DEV_MODE) {
            getMethodNames(new Throwable().getStackTrace());
            Log.i(className, createLog(""));
        }
    }

    public static void d(String msg) {
        if (DEV_MODE) {
            getMethodNames(new Throwable().getStackTrace());
            Log.d(className, createLog(msg));
        }
    }

    public static void e(String tag, String msg) {
        if (DEV_MODE) {
            getMethodNames(new Throwable().getStackTrace());
            Log.e(tag, createLog(msg));
        }
    }

    public static void w(String tag, String msg) {
        if (DEV_MODE) {
            getMethodNames(new Throwable().getStackTrace());
            Log.w(tag, createLog(msg));
        }
    }

    public static void i(String tag, String msg) {
        if (DEV_MODE) {
            getMethodNames(new Throwable().getStackTrace());
            Lawg.i(createLog(msg));
        }
    }

    public static void d(String tag, String msg) {
        if (DEV_MODE) {
            getMethodNames(new Throwable().getStackTrace());
            Log.d(tag, createLog(msg));
        }
    }

    public static void printBundle(Bundle bundle) {
        if (DEV_MODE) {

            for (String key : bundle.keySet()) {

                Object value = bundle.get(key);
                String msg = String.format("%s %s (%s)", key,
                        value.toString(), value.getClass().getName());

                getMethodNames(new Throwable().getStackTrace());
                Log.i(className, createLog(msg));
            }
        }
    }

    public static String stackTraceToString(Throwable error) {
        StringWriter errors = new StringWriter();
        error.printStackTrace(new PrintWriter(errors));
        return errors.toString();
    }
}
