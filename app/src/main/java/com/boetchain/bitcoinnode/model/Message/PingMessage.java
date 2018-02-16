package com.boetchain.bitcoinnode.model.Message;

import android.content.Context;

import com.boetchain.bitcoinnode.R;

import java.util.Random;

/**
 * Created by rossbadenhorst on 2018/02/06.
 */

public class PingMessage extends BaseMessage {

    public static final String COMMAND_NAME = "ping";
    public long nonce;

    public PingMessage() {
        super();
        this.nonce = new Random().nextLong();

        writePayload();
        writeHeader();
    }

    public PingMessage(byte[] header, byte[] payload) {
        super(header, payload);
    }

    @Override
    public String getCommandName() {
        return COMMAND_NAME;
    }

    @Override
    public String getHumanReadableCommand(Context context) {
        return context.getString(R.string.command_ping_message_1);
    }

    @Override
    protected void readPayload() {
        this.nonce = readUint64().longValue();
    }

    @Override
    protected void writePayload() {
        writeUint64(this.nonce);

        payload = new byte[outputPayload.size()];
        for (int i = 0; i < payload.length && i < outputPayload.size(); i++) {
            payload[i] = outputPayload.get(i).byteValue();
        }
    }
}
