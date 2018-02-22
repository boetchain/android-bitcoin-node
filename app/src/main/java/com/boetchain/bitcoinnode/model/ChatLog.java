package com.boetchain.bitcoinnode.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Tyler Hogarth on 2018/02/08.
 */

public class ChatLog implements Parcelable, Comparable<ChatLog> {

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
     * The text to display in a message.
     * Best display something here that is human readable.
     */
    public String text = "";
    /**
     * The command correlating to the message.
     * We normaly pump out the BTC protocol command here like: getadd or sendcmpt.
     */
    public String command = "";
    /**
     * The time this message was recieved or sent.
     * Human readable format.
     */
    public String time;
    /**
     * Time stamp for the message.
     * Used to order the messages.
     */
    public long timestamp;
    /**
     * The tyoe of message.
     * Allows us to seperate incomming, outgoing and neutral.
     */
    public int type = TYPE_NEUTRAL;

    public ChatLog(String text, int type) {
        this.text = text;
        this.time = getHumanReadableTime(System.currentTimeMillis());
        this.timestamp = System.currentTimeMillis();
        this.type = type;
    }

    public ChatLog(String text, String command, long time, int type) {
        this.text = text;
        this.command = command;
        this.time = getHumanReadableTime(time);
        this.timestamp = time;
        this.type = type;
    }

    /**
     * Converts a time stamp to a nice human readale form.
     * Used for display purposes.
     * @param time - timestamp (epoch in millies.)
     * @return human readable form.
     */
    public static String getHumanReadableTime(long time) {
        return new SimpleDateFormat("HH:mm").format(new Date(time));
    }

    protected ChatLog(Parcel in) {
        text = in.readString();
        command = in.readString();
        time = in.readString();
        timestamp = in.readLong();
        type = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(text);
        dest.writeString(command);
        dest.writeString(time);
        dest.writeLong(timestamp);
        dest.writeInt(type);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<ChatLog> CREATOR = new Parcelable.Creator<ChatLog>() {
        @Override
        public ChatLog createFromParcel(Parcel in) {
            return new ChatLog(in);
        }

        @Override
        public ChatLog[] newArray(int size) {
            return new ChatLog[size];
        }
    };

    @Override
    public int compareTo(@NonNull ChatLog chatLog) {
        return (int) (this.timestamp - chatLog.timestamp);
    }
}
