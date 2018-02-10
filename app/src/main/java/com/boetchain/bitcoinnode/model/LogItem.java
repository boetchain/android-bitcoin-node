package com.boetchain.bitcoinnode.model;

/**
 * Created by Tyler Hogarth on 2018/02/08.
 */

public class LogItem {

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

    public int type = TI;
    public String text = "";

    public LogItem(int type, String msg) {
        this.type = type;
        text = msg;
    }
}
