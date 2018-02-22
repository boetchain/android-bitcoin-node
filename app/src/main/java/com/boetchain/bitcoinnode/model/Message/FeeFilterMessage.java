package com.boetchain.bitcoinnode.model.Message;

import android.content.Context;

import com.boetchain.bitcoinnode.R;

/**
 * Created by Ross Badenhorst.
 */
public class FeeFilterMessage extends BaseMessage {

    public static final String COMMAND_NAME = "feefilter";
    /**
     * The minimum fee in satoshis per 1000 bytes.
     * Upon receipt of a "feefilter" message, the node will be permitted, but not required,
     * to filter transaction invs for transactions that fall below the feerate provided in
     * the feefilter message interpreted as satoshis per kilobyte
     */
    public long feerate;

    public FeeFilterMessage() {
        super();

        writePayload();
        writeHeader();
    }

    public FeeFilterMessage(byte[] header, byte[] payload) {
        super(header, payload);
    }

    @Override
    public String getCommandName() {
        return COMMAND_NAME;
    }

    @Override
    public String getHumanReadableCommand(Context context) {
        return context.getString(R.string.command_feefilter_message_1).replace("{:value}", "" + feerate);
    }

    @Override
    protected void readPayload() {
        feerate = readUint64().longValue();
    }

    @Override
    protected void writePayload() {

        payload = new byte[outputPayload.size()];
        for (int i = 0; i < payload.length && i < outputPayload.size(); i++) {
            payload[i] = outputPayload.get(i).byteValue();
        }
    }
}
