package com.boetcoin.bitcoinnode.model.Message;

import com.boetcoin.bitcoinnode.model.Peer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rossbadenhorst on 2018/02/06.
 */

public class VerAckMessage extends BaseMessage {

    public static final String COMMAND_NAME = "verack";

    /**
     * The max number of addresses allowed in the list
     * according to the BTC protocol.
     */
    public static final int MAX_ADDRESSES = 1000;

    /**
     * The number of addresses in the list.
     * The list should be the next item after the count.
     * Works in a similar way to a String does.
     */
    public int count;
    public List<Peer> addrList;

    public VerAckMessage() {
        super();
        addrList = new ArrayList<>();
        this.count = addrList.size();

        writePayload();
        writeHeader();
    }

    public VerAckMessage(byte[] header, byte[] payload) {
        super(header, payload);
    }

    @Override
    public String getCommandName() {
        return COMMAND_NAME;
    }

    @Override
    protected void readPayload() {
        if (payload.length == 0) {
            return;
        }

        this.count = (int) readVarInt();
        this.addrList = readAddressList();
    }

    @Override
    protected void writePayload() {
        writeVarInt(count);
        writeAddressList(addrList);

        payload = new byte[outputPayload.size()];
        for (int i = 0; i < payload.length && i < outputPayload.size(); i++) {
            payload[i] = outputPayload.get(i).byteValue();
        }
    }

    private List<Peer> readAddressList() {
        return new ArrayList<>();
    }

    private void writeAddressList(List<Peer> addrList) {

    }

    @Override
    public String toString() {
        return "VerAckMessage{" +
                "count=" + count +
                ", addrList=" + addrList +
                '}';
    }
}
