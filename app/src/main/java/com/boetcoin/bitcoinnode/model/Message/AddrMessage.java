package com.boetcoin.bitcoinnode.model.Message;

import android.util.Log;

import com.boetcoin.bitcoinnode.App;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Tyler Hogarth on 2018/02/07.
 */

public class AddrMessage extends BaseMessage {

    public static final String COMMAND_NAME = "addr";

    public int count;
    public List<String> addresses;

    public AddrMessage() {
        super();

        writePayload();
        writeHeader();
    }

    public AddrMessage(byte[] header, byte[] payload) {
        super(header, payload);
    }

    @Override
    public String getCommandName() {
        return COMMAND_NAME;
    }

    @Override
    protected void readPayload() {

        long numAddresses = readVarInt();
        addresses = new ArrayList<>((int) numAddresses);
        BigInteger services;
        InetAddress addr;
        int port;

        long time;
        for (int i = 0; i < numAddresses; i++) {
            time = readUint32();
            services = readUint64();
            byte[] addrBytes = readBytes(16);
            try {
                addr = InetAddress.getByAddress(addrBytes);
            } catch (UnknownHostException e) {
                throw new RuntimeException(e);  // Cannot happen.
            }
            port = ((0xFF & payload[cursor++]) << 8) | (0xFF & payload[cursor++]);

            Log.i(App.TAG, "Time: " + time);
            Log.i(App.TAG, "Address: " + addr.toString());
            Log.i(App.TAG, "Services: " + services.toString());
            Log.i(App.TAG, "Port: " + port);
            Log.i(App.TAG, "--------------------------------");
            // The 4 byte difference is the uint32 timestamp that was introduced in version 31402
            //length = protocolVersion > 31402 ? MESSAGE_SIZE : MESSAGE_SIZE - 4;
        }

    }

    @Override
    protected void writePayload() {

        payload = new byte[outputPayload.size()];
        for (int i = 0; i < payload.length && i < outputPayload.size(); i++) {
            payload[i] = outputPayload.get(i).byteValue();
        }
    }
}
