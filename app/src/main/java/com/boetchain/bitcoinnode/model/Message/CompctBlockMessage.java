package com.boetchain.bitcoinnode.model.Message;

import android.content.Context;

import com.boetchain.bitcoinnode.R;

/**
 * Created by rossbadenhorst on 2018/02/07.
 */

public class CompctBlockMessage extends BaseMessage {

    public static final String COMMAND_NAME = "cmpctblock";

    public CompctBlockMessage() {
        super();

        writePayload();
        writeHeader();
    }

    public CompctBlockMessage(byte[] header, byte[] payload) {
        super(header, payload);
    }

    @Override
    public String getCommandName() {
        return COMMAND_NAME;
    }

    @Override
    public String getHumanReadableCommand(Context context) {
        return context.getString(R.string.command_compct_block_message_1);
    }

    @Override
    protected void readPayload() {

    }

    @Override
    protected void writePayload() {

        writeInt(0);

        payload = new byte[outputPayload.size()];
        for (int i = 0; i < payload.length && i < outputPayload.size(); i++) {
            payload[i] = outputPayload.get(i).byteValue();
        }
    }
}
