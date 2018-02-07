package com.boetcoin.bitcoinnode.model.Message;

/**
 * Created by rossbadenhorst on 2018/02/07.
 */

public class SendCmpctMessage extends BaseMessage {

    public static final String COMMAND_NAME = "sendcmpct";

    boolean usecompact;

    public SendCmpctMessage() {
        super();

        this.usecompact = writeBytes();

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
    protected void readPayload() {

    }

    @Override
    protected void writePayload() {

        writeBytes(new byte[] {0});
        writeUint64(70015);

        payload = new byte[outputPayload.size()];
        for (int i = 0; i < payload.length && i < outputPayload.size(); i++) {
            payload[i] = outputPayload.get(i).byteValue();
        }
    }
}
