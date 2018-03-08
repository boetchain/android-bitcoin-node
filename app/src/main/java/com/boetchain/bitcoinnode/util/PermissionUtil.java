package com.boetchain.bitcoinnode.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

/**
 * Created by Tyler Hogarth on 16/06/06.
 */
public class PermissionUtil {

    public static final int REQUEST_CODE_MAP = 1;

    public static boolean canUseCoarseLocation(Context context) {
        return ContextCompat.checkSelfPermission(context,
                                                 Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean canUseFineLocation(Context context) {
        return ContextCompat.checkSelfPermission(context,
                                                 Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean requestLocationPermission(Activity activity) {
        if (!canUseFineLocation(activity) || !canUseCoarseLocation(activity)) {

            ActivityCompat.requestPermissions(activity,
                                              new String[]{
		                                              Manifest.permission.ACCESS_COARSE_LOCATION,
		                                              Manifest.permission.ACCESS_FINE_LOCATION},
                                              REQUEST_CODE_MAP);
            return true;
        }
        return false;
    }
}
