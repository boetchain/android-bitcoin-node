package com.boetchain.bitcoinnode.model.Message;

/**
 * Created by rossbadenhorst on 2018/02/07.
 */

public class HeadersMessage extends BaseMessage {

    public static final String COMMAND_NAME = "headers";

    public HeadersMessage() {
        super();

        writePayload();
        writeHeader();
    }

    public HeadersMessage(byte[] header, byte[] payload) {
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

        writeInt(0);

        payload = new byte[outputPayload.size()];
        for (int i = 0; i < payload.length && i < outputPayload.size(); i++) {
            payload[i] = outputPayload.get(i).byteValue();
        }
    }
}
