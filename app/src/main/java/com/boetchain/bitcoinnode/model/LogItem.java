package com.boetchain.bitcoinnode.model;

/**
 * Created by Tyler Hogarth on 2018/02/08.
 */

public class LogItem {

    /**
     * This will determine where a log is displayed. Status/neutral messages
     * Display: Center
     */
    public static final int TYPE_NEUTRAL = 0;
    /**
     * This will determine where a log is displayed. Incoming messages
     * Display: Left
     */
    public static final int TYPE_IN = 1;

    /**
     * This will determine where a log is displayed. Outgoing messages
     * Display: Right
     */
    public static final int TYPE_OUT = 2;

    /**
     * UI Log type INFO
     */
    public static final int TI = 0;

    /**
     * UI Log type VERBOSE
     */
    public static final int TW = 1;

    /**
     * UI Log type WARNING
     */
    public static final int TD = 2;

    /**
     * UI Log type DEBUG
     */
    public static final int TE = 3;

    /**
     * UI Log type ERROR
     */
    public static final int TV = 4;

    public String text = "";
    public int type = TYPE_NEUTRAL;
    public int log = TI;

    public LogItem(String text, int type, int log) {
        this.text = text;
        this.type = type;
        this.log = log;
    }
}
