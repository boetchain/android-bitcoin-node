package com.boetchain.bitcoinnode.model.Message;

import com.boetchain.bitcoinnode.model.Peer;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Tyler Hogarth on 2018/02/07.
 */

public class AddrMessage extends BaseMessage {

    public static final String COMMAND_NAME = "addr";

    /**
     * How many peers are in the address list.
     * Used to tell use how far down the payload we need to read.
     */
    public int count;
    /**
     * List of peers.
     */
    public List<Peer> addresses;

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
        long numberOfAddresses = readVarInt();
        addresses = readAddresses(numberOfAddresses);
    }

    /**
     * Reads an address list send to us
     *
     * @param numberOfAddresses - how many peers are in the address book :P
     * @return - nicely read peers for us to use.
     */
    private List<Peer> readAddresses(long numberOfAddresses) {
        addresses = new ArrayList<>();

        for (int i = 0; i < numberOfAddresses; i++) {
            addresses.add(readPeer());
        }

        return addresses;
    }

    /**
     * Reads a single peer from the payload.
     *
     * @return - a peer.
     */
    private Peer readPeer() {
        // The address of the peer
        InetAddress address;
        // The port of the peer
        int port;
        // The services the peer offers
        long services;
        // The last time this peer was contacted
        long time;

        time = readUint32();
        services = readUint64().longValue();

        byte[] addrBytes = readBytes(16);
        try {
            address = InetAddress.getByAddress(addrBytes);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);  // Cannot happen.
        }
        port = ((0xFF & payload[cursor++]) << 8) | (0xFF & payload[cursor++]);

        return new Peer(address.getHostAddress(), port, services, time);
    }

    @Override
    protected void writePayload() {

        payload = new byte[outputPayload.size()];
        for (int i = 0; i < payload.length && i < outputPayload.size(); i++) {
            payload[i] = outputPayload.get(i).byteValue();
        }
    }
}
