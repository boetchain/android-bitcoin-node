package com.boetcoin.bitcoinnode.model.Message;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by rossbadenhorst on 2018/02/06.
 */

public class AlertMessage extends BaseMessage {

    public static final String COMMAND_NAME = "alert";

    private byte[] content;
    private byte[] signature;

    private int version;
    private long relayUntil;
    private long expiration;
    private int id;
    private int cancel;
    private Set<Long> setCancel;
    private long minVer;
    private long maxVer;
    private Set<String> setSubVer;
    private int priority;
    private String comment;
    private String statusBar;
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
