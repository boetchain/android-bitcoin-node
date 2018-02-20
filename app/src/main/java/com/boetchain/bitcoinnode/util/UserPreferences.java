package com.boetchain.bitcoinnode.util;

import android.content.Context;
import android.content.SharedPreferences;

public class UserPreferences {

    public static final String PREFS = "com.boetchain.bitcoinnode.";

    private static SharedPreferences sp;
    private static SharedPreferences.Editor spEditor;

    /**
     * If the user turns on the PeerManagementService this pref is set to true
     */
    public static final String PEER_MANAGEMENT_SERVICE_ON = "PEER_MANAGEMENT_SERVICE_ON";           //boolean

    /**
     * Sets the values of a shared preference for a specific key.
     *
     * @param c     - Context in which this is called.
     * @param key   - The key of the shared preference to save.
     * @param value - The value to store.
     */
    public static void setString(Context c,
                                 String key,
                                 String value) {

        sp = getSharedPreferences(c);

        spEditor = sp.edit();

        spEditor.putString(key, value);

        spEditor.commit();

        sp = null;
    }

    /**
     * Gets the value of the stored shared preference.
     *
     * @param c   - Context in which this is called.
     * @param key - The key of the shared preference to save.
     * @return        - The stored value.
     */
    public static String getString(Context c,
                                   String key, String defaultValue) {

        sp = getSharedPreferences(c);

        String pref = sp.getString(key, defaultValue);

        sp = null;

        return pref;
    }

    /**
     * Sets the values of a shared preference for a specific key.
     *
     * @param c     - Context in which this is called.
     * @param key   - The key of the shared preference to save.
     * @param value - The value to store.
     */
    public static void setBoolean(Context c,
                                  String key,
                                  boolean value) {

        sp = getSharedPreferences(c);

        spEditor = sp.edit();

        spEditor.putBoolean(key, value);

        spEditor.commit();

        sp = null;
    }

    /**
     * Gets the value of the stored shared preference as boolean.
     *
     * @param c   - Context in which this is called.
     * @param key - The key of the shared preference to save.
     * @return        - The stored value.
     */
    public static boolean getBoolean(Context c,
                                     String key, boolean defaultValue) {

        sp = getSharedPreferences(c);

        boolean pref = sp.getBoolean(key, defaultValue);

        sp = null;

        return pref;
    }

    /**
     * Sets the values of a shared preference for a specific key.
     *
     * @param c     - Context in which this is called.
     * @param key   - The key of the shared preference to save.
     * @param value - The value to store.
     */
    public static void setInteger(Context c,
                                  String key,
                                  int value) {

        sp = getSharedPreferences(c);

        spEditor = sp.edit();

        spEditor.putInt(key, value);

        spEditor.commit();

        sp = null;
    }

    /**
     * Gets the value of the stored shared preference as boolean.
     *
     * @param c   - Context in which this is called.
     * @param key - The key of the shared preference to save.
     * @return        - The stored value.
     */
    public static int getInteger(Context c,
                                 String key, int defualtValue) {

        sp = getSharedPreferences(c);

        int pref = sp.getInt(key, defualtValue);

        sp = null;

        return pref;
    }

    /**
     * Sets the values of a shared preference for a specific key.
     *
     * @param c     - Context in which this is called.
     * @param key   - The key of the shared preference to save.
     * @param value - The value to store.
     */
    public static void setLong(Context c,
                               String key,
                               long value) {

        sp = getSharedPreferences(c);

        spEditor = sp.edit();

        spEditor.putLong(key, value);

        spEditor.commit();

        sp = null;
    }

    /**
     * Gets the value of the stored shared preference as boolean.
     *
     * @param c   - Context in which this is called.
     * @param key - The key of the shared preference to save.
     * @return        - The stored value.
     */
    public static long getLong(Context c,
                               String key, long defualtValue) {

        sp = getSharedPreferences(c);

        long pref = sp.getLong(key, defualtValue);

        sp = null;

        return pref;
    }

    public static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }
}
