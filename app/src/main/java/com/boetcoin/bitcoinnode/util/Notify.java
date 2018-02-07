package com.boetcoin.bitcoinnode.util;

import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;

/**
 * Created by Tyler on 2015-11-05.
 */
public class Notify {

    public static void toast(Context context, int resId, int duration) {

        toast(context, context.getResources().getString(resId), duration);
    }

    public static void toast(Context context, String msg, int duration) {

        Toast toast = Toast.makeText(context, msg, duration);
        toast.setGravity(Gravity.TOP, 0, 200);
        toast.show();
    }

    public static void toastBottom(Context context, int resId, int duration) {

        toastBottom(context, context.getResources().getString(resId), duration);
    }

    public static void toastBottom(Context context, String msg, int duration) {

        Toast toast = Toast.makeText(context, msg, duration);
        toast.setGravity(Gravity.BOTTOM, 0, 200);
        toast.show();
    }
}