package com.boetchain.bitcoinnode.model.Message;

import android.content.Context;

import com.boetchain.bitcoinnode.R;

import java.util.Random;

/**
 * Created by Ross Badenhorst.
 */
public class InvMessage extends BaseMessage {

    public static final String COMMAND_NAME = "inv";

    public InvMessage() {
        super();

        writePayload();
        writeHeader();
    }

    public InvMessage(byte[] header, byte[] payload) {
        super(header, payload);
    }

    @Override
    public String getCommandName() {
        return COMMAND_NAME;
    }

    @Override
    public String getHumanReadableCommand(Context context) {
        switch (new Random().nextInt(3)) {
            case 0:
                return context.getString(R.string.command_inv_message_1);
            case 1:
                return context.getString(R.string.command_inv_message_2);
            case 2:
                return context.getString(R.string.command_inv_message_3);
            case 3:
                return context.getString(R.string.command_inv_message_4);
            default:
                return context.getString(R.string.command_inv_message_1);
        }
    }

    @Override
    protected void readPayload() {
    }

    @Override
    protected void writePayload() {
        payload = new byte[outputPayload.size()];
        for (int i = 0; i < payload.length && i < outputPayload.size(); i++) {
            payload[i] = outputPayload.get(i).byteValue();
        }
    }
}
