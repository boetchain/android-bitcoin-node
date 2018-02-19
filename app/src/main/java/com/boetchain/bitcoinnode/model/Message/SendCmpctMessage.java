package com.boetchain.bitcoinnode.model.Message;

import android.content.Context;

import com.boetchain.bitcoinnode.R;

/**
 * Created by rossbadenhorst on 2018/02/07.
 */

public class SendCmpctMessage extends BaseMessage {

    public static final String COMMAND_NAME = "sendcmpct";

    boolean usecompact;
    long version;

    public SendCmpctMessage() {
        super();

        this.usecompact = false;
        this.version  = 70015;

        writePayload();
        writeHeader();
    }

    public SendCmpctMessage(byte[] header, byte[] payload) {
        super(header, payload);
    }

    @Override
    public String getCommandName() {
        return COMMAND_NAME;
    }

    @Override
    public String getHumanReadableCommand(Context context) {
        return context.getString(R.string.command_send_compct_message_1);
    }

    @Override
    protected void readPayload() {
        this.usecompact = readBoolean();
        this.version = readUint64().longValue();
    }

    @Override
    protected void writePayload() {

        writeBoolean(usecompact);
        writeUint64(version);

        payload = new byte[outputPayload.size()];
        for (int i = 0; i < payload.length && i < outputPayload.size(); i++) {
            payload[i] = outputPayload.get(i).byteValue();
        }
    }

    @Override
    public String toString() {
        return "SendCmpctMessage{" +
                "usecompact=" + usecompact +
                ", version=" + version +
                '}';
    }
}
