package com.boetcoin.bitcoinnode.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Base64;

/**
 * Created by rossbadenhorst on 2018/01/31.
 */

public class Prefs {

    /**
     * Saves a byte array in the Shared Preferences.
     *
     * The byte array is converted to a String before storage
     * as Android does not support storing byte arrays.
     *
     * @param context   - of the Application.
     * @param key       - The name of the preference to saveEventDataRecord.
     * @param value     - value to saveEventDataRecord.
     */
    public static void putByte(Context context, String key, byte[] value) {
        if (getSharedPreferences(context) != null){
            SharedPreferences.Editor prefs = getSharedPreferences(context).edit();
            String byteString = Base64.encodeToString(value, Base64.NO_WRAP);
            prefs.putString(key, byteString);
            prefs.commit();
        }
    }

    /**
     * Helper method to retrieve a String value from {@link SharedPreferences}.
     *
     * @param context a {@link Context} object.
     * @param key
     * @return The value from shared preferences, or null if the value could not be read.
     */
    public static long getLong(Context context, String key) {
        SharedPreferences preferences = getSharedPreferences(context);
        if (preferences != null) {
            return preferences.getLong(key, 0);
        }

        return 0;
    }

    /**
     * Helper method to write a int value to {@link SharedPreferences}.
     *
     * @param context a {@link Context} object.
     * @param key
     * @param value
     * @return true if the new value was successfully written to persistent storage.
     */
    public static boolean put(Context context, String key, long value) {
        SharedPreferences preferences = getSharedPreferences(context);
        if (preferences != null && !TextUtils.isEmpty(key)) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putLong(key, value);
            return editor.commit();
        }
        return false;
    }

    /**
     * Saves a byte array in the Shared Preferences.
     *
     * The byte array is converted to a String before storage
     * as Android does not support storing byte arrays.
     *
     * @param context   - of the Application.
     * @param value     - value to saveEventDataRecord.
     */
    public static void put(Context context, String key, byte[] value) {
        if (getSharedPreferences(context) != null) {
            SharedPreferences.Editor prefs = getSharedPreferences(context).edit();
            String byteString = Base64.encodeToString(value, Base64.NO_WRAP);
            prefs.putString(key, byteString);
            prefs.commit();
        }
    }

    /**
     * Gets a byte array in the Shared Preferences.
     *
     * @param context   - of the Application
     * @param key       - The name of the preference to retrieve.
     * @param defValue  - Value to return if this preference does not exist.
     * @return the preference value if it exists, or defValue.
     */
    public static byte[] getByte(Context context, String key, byte[] defValue) {
        SharedPreferences prefs =  getSharedPreferences(context);
        if (prefs != null) {
            String byteString = prefs.getString(key, Base64.encodeToString(defValue, Base64.NO_WRAP));
            return Base64.decode(byteString, Base64.NO_WRAP);
        }
        return null;
    }

    /**
     * Helper method to retrieve a String value from {@link SharedPreferences}.
     *
     * @param context a {@link Context} object.
     * @param key
     * @return The value from shared preferences, or null if the value could not be read.
     */
    public static String getStr(Context context, String key) {
        String value = null;
        SharedPreferences preferences = getSharedPreferences(context);
        if (preferences != null) {
            value = preferences.getString(key, "");
        }
        return value;
    }

    /**
     * Helper method to retrieve a String value from {@link SharedPreferences}.
     *
     * @param context a {@link Context} object.
     * @param key
     * @return The value from shared preferences, or null if the value could not be read.
     */
    public static int getInt(Context context, String key) {
        int value = 0;
        SharedPreferences preferences = getSharedPreferences(context);
        if (preferences != null) {
            value = preferences.getInt(key, 0);
        }
        return value;
    }

    /**
     * Helper method to retrieve a String value from {@link SharedPreferences}.
     *
     * @param context a {@link Context} object.
     * @param key
     * @return The value from shared preferences, or null if the value could not be read.
     */
    public static int getInt(Context context, String key, int defaultValue) {
        int value = defaultValue;
        SharedPreferences preferences = getSharedPreferences(context);
        if (preferences != null) {
            value = preferences.getInt(key, defaultValue);
        }
        return value;
    }

    /**
     * Helper method to retrieve a String value from {@link SharedPreferences}.
     *
     * @param context a {@link Context} object.
     * @param key
     * @return The value from shared preferences, or null if the value could not be read.
     */
    public static boolean getBool(Context context, String key) {
        boolean value = false;
        SharedPreferences preferences = getSharedPreferences(context);
        if (preferences != null) {
            value = preferences.getBoolean(key, false);
        }
        return value;
    }

    /**
     * Helper method to write a String value to {@link SharedPreferences}.
     *
     * @param context a {@link Context} object.
     * @param key
     * @param value
     * @return true if the new value was successfully written to persistent storage.
     */
    public static boolean put(Context context, String key, String value) {
        SharedPreferences preferences = getSharedPreferences(context);
        if (preferences != null && !TextUtils.isEmpty(key)) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(key, value);
            return editor.commit();
        }
        return false;
    }

    /**
     * Helper method to write a int value to {@link SharedPreferences}.
     *
     * @param context a {@link Context} object.
     * @param key
     * @param value
     * @return true if the new value was successfully written to persistent storage.
     */
    public static boolean put(Context context, String key, int value) {
        SharedPreferences preferences = getSharedPreferences(context);
        if (preferences != null && !TextUtils.isEmpty(key)) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putInt(key, value);
            return editor.commit();
        }
        return false;
    }

    /**
     * Helper method to write a String value to {@link SharedPreferences}.
     *
     * @param context a {@link Context} object.
     * @param key
     * @param value
     * @return true if the new value was successfully written to persistent storage.
     */
    public static boolean put(Context context, String key, boolean value) {
        SharedPreferences preferences = getSharedPreferences(context);
        if (preferences != null && !TextUtils.isEmpty(key)) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean(key, value);
            return editor.commit();
        }
        return false;
    }

    /**
     * Gets the default shared prefs for the app.
     * @param context
     * @return
     */
    private static SharedPreferences getSharedPreferences(Context context) {

        if (context != null) {
            return PreferenceManager.getDefaultSharedPreferences(context);
        }
        return null;
    }
}
