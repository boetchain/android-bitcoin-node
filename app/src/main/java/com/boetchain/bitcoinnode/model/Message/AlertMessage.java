package com.boetchain.bitcoinnode.model.Message;

import android.content.Context;

import com.boetchain.bitcoinnode.R;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by rossbadenhorst on 2018/02/06.
 */

public class AlertMessage extends BaseMessage {

    public static final String COMMAND_NAME = "alert";

    private byte[] content;
    private byte[] signature;

    /**
     * Alert format version
     */
    private int version;
    /**
     * The timestamp beyond which nodes should stop relaying this alert
     */
    private long relayUntil;
    /**
     * 	The timestamp beyond which this alert is no longer in effect and should be ignored
     */
    private long expiration;
    /**
     * 	A unique ID number for this alert
     */
    private int id;
    /**
     * 	All alerts with an ID number less than or equal to this number should be cancelled: deleted and not accepted in the future
     */
    private int cancel;
    /**
     * All alert IDs contained in this set should be cancelled as above
     */
    private Set<Long> setCancel;
    /**
     * This alert only applies to versions greater than or equal to this version. Other versions should still relay it.
     */
    private long minVer;
    /**
     * 	This alert only applies to versions less than or equal to this version. Other versions should still relay it.
     */
    private long maxVer;
    /**
     * If this set contains any elements, then only nodes that have their subVer contained in this set are affected by the alert. Other versions should still relay it.
     */
    private Set<String> setSubVer;
    /**
     * Relative priority compared to other alerts
     */
    private int priority;
    /**
     * A comment on the alert that is not displayed
     */
    private String comment;
    /**
     * 	The alert message that is displayed to the user
     */
    private String statusBar;
    /**
     * 	Reserved
     */
    private String reserved;

    public AlertMessage() {
        super();
    }

    public AlertMessage(byte[] header, byte[] payload) {
        super(header, payload);
    }

    @Override
    public String getCommandName() {
        return COMMAND_NAME;
    }

    @Override
    public String getHumanReadableCommand(Context context) {
        return context.getString(R.string.command_alert_message_1);
    }

    @Override
    protected void readPayload() {

        int startPos = cursor;

        this.content = readByteArray();
        this.signature = readByteArray();

        // Figure out the actual structure in the two sub arrays
        cursor = startPos; // Reset the cursor

        readVarInt();  // Skip the length field on the content array.
        this.version = (int) readUint32();
        this.relayUntil = readUint64().longValue();
        this.expiration = readUint64().longValue();
        this.id = (int) readUint32();
        this.cancel = (int) readUint32();

        long setCancelSize = readVarInt();
        this.setCancel =  new HashSet<>((int) setCancelSize);
        for (long i = 0; i < setCancelSize; i++) {
            setCancel.add(readUint32());
        }

        minVer = readUint32();
        maxVer = readUint32();

        long setSubVerSize = readVarInt();
        this.setSubVer = new HashSet<>((int) setSubVerSize);
        for (long i = 0; i < setSubVerSize; i++) {
            setSubVer.add(readStr());
        }

        this.priority = (int) readUint32();
        comment = readStr();
        statusBar = readStr();
        reserved = readStr();
    }

    @Override
    protected void writePayload() {
        payload = new byte[outputPayload.size()];
        for (int i = 0; i < payload.length && i < outputPayload.size(); i++) {
            payload[i] = outputPayload.get(i).byteValue();
        }
    }

    @Override
    public String toString() {
        return "AlertMessage{" +
                ", version=" + version +
                ", relayUntil=" + relayUntil +
                ", expiration=" + expiration +
                ", id=" + id +
                ", cancel=" + cancel +
                ", setCancel=" + setCancel +
                ", minVer=" + minVer +
                ", maxVer=" + maxVer +
                ", setSubVer=" + setSubVer +
                ", priority=" + priority +
                ", comment='" + comment + '\'' +
                ", statusBar='" + statusBar + '\'' +
                ", reserved='" + reserved + '\'' +
                '}';
    }
}
