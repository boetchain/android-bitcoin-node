package com.boetchain.bitcoinnode.model.Message;

import android.content.Context;

import com.boetchain.bitcoinnode.R;

/**
 * Created by Ross Badenhorst.
 */
public class PongMessage extends BaseMessage {

    public static final String COMMAND_NAME = "pong";
    /**
     * Random number send with every ping.
     * If you send out a ping, you can see if you are pinging yourself - if you get a incoming ping
     * with the same random number you sent...
     *
     * Happens if your behind some funny proxy or something...
     */
    public long nonce;

    public PongMessage() {
        super();

        writePayload();
        writeHeader();
    }

    public PongMessage(byte[] header, byte[] payload) {
        super(header, payload);
    }

    @Override
    public String getCommandName() {
        return COMMAND_NAME;
    }

    @Override
    public String getHumanReadableCommand(Context context) {
        return context.getString(R.string.command_pong_message_1);
    }

    @Override
    protected void readPayload() {
        nonce = readUint64().longValue();
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
