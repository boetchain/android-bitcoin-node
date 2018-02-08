package com.boetcoin.bitcoinnode.model.Message;

/**
 * Created by Tyler Hogarth on 2018/02/08.
 */

public class PongMessage extends BaseMessage {

    public static final String COMMAND_NAME = "pong";

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
