package com.boetchain.bitcoinnode.util;

import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.provider.Settings;
import android.view.Display;
import android.view.Gravity;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

/**
 * Created by Tyler on 2015-10-26.
 */
public class DeviceUtil
{

    public static final String OS = "android";

    /**
     * Determines whether the device is connected to the internet.
     * @return true if there is a connection to the net
     */
    public static boolean isInternetConnection(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    /**
     * Determines whether network (cell towers and wifi) location is on.
     * @param context
     * @return true if device network location is on.
     */
    public static boolean isNetworkLocationOn(Context context) {

        LocationManager locMan = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        return locMan.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    public static boolean isGpsLocationOn(Context context) {

        LocationManager locMan = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        return locMan.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    public static boolean isLocationOn(Context context) {
        LocationManager locMan = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        return locMan.isProviderEnabled(LocationManager.GPS_PROVIDER) &&
               locMan.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    public static boolean isMockLocationOn(Context context, Location location) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            if (location != null) {
                return location.isFromMockProvider();
            } else {
                return false;
            }
        } else {

            // ALLOW_MOCK_LOCATION deprecated in api 23 (Marshmallow)
            // returns true if mock location enabled, false if not enabled.
            if (Settings.Secure.getString(context.getContentResolver(),
                                          Settings.Secure.ALLOW_MOCK_LOCATION).equals("0"))
                return false;
            else
                return true;
        }
    }

    public static int getScreenWidth(Activity activity) {
        Display display = activity.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size.x;
    }

    public static int getScreenHeight(Activity activity) {
        Display display = activity.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size.y;
    }

    /**
     * Gets the screen height and width and stores it in a Point object
     * @param activity
     * @return
     */
    public static Point getScreenPoint(Activity activity) {
        Display display = activity.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size;
    }

    /**
     * A 64-bit number (as a hex string) that is randomly generated when the user first sets up the
     * device and should remain constant for the lifetime of the user's device. The value may change
     * if a factory reset is performed on the device.
     *
     * Note: When a device has multiple users (available on certain devices running Android 4.2 or
     * higher), each user appears as a completely separate device, so the ANDROID_ID value is unique
     * to each user.
     *
     * @param context
     * @return
     */
    public static String getAndroidId(Context context) {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    public static String getAnonymousId(Context context) {
        return getAndroidId(context);
    }

    public static String getOS() {

        return OS;
    }

    public static String getOSVersion() {
        String version = Build.VERSION.RELEASE;
        if (version == null) {
            return "";
        }
        return version;
    }

    public static String getAppVersionName(Context context) {

        String versionStr = "";
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            versionStr = pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return versionStr;
    }

    public static int getAppVersionCode(Context context) {

        int versionCode = -1;
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            versionCode = pInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return versionCode;
    }

    public static String getMake() {
        String make = Build.MANUFACTURER;
        if (make == null) {
            return "";
        }
        return make;
    }

    public static String getModel() {
        String model = Build.MODEL;
        if (model == null) {
            return "";
        }
        return model;
    }

    /**
     * Checks Google Play Services and creates a dialog with the appropriate solution if the
     * user does not have it installed
     *
     * @param activity
     * @return true if the user has GPS installed
     */
    public static boolean checkPlayServices(final Activity activity) {

        int result = GooglePlayServicesUtil.isGooglePlayServicesAvailable(activity);
        if (result != ConnectionResult.SUCCESS) {
            GooglePlayServicesUtil
                    .getErrorDialog(result,
                                    activity,
                                    -1,
                                    new DialogInterface.OnCancelListener() {
                                        @Override
                                        public void onCancel(DialogInterface dialog) {
                                            activity.finish();
                                        }
                                    }).show();
            return false;
        }
        return true;
    }

    public static void copyToClipBoard(Context context, String text, boolean notify) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
            android.content.ClipboardManager cb = (android.content.ClipboardManager) context.getSystemService(
		            Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("custom_text_copy_516", text);
            cb.setPrimaryClip(clip);
            if (notify) {
                Toast toast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.TOP, 0, 200);
                toast.show();
            }
        }
    }
}
