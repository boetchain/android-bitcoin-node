package com.boetchain.bitcoinnode.model;

/**
 * Created by Tyler Hogarth on 2018/02/08.
 */

public class ChatLog {

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

    public String text = "";
    public String command = "";
    public String time;
    public int type = TYPE_NEUTRAL;

    public ChatLog(String text, String command, long time, int type) {
        this.text = text;
        this.command = command;
        this.time = getHumanReadableTime(time);
        this.type = type;
    }

    public static String getHumanReadableTime(long time) {
        //todo return proper time
        return "22:00";
    }
}
