package com.boetcoin.bitcoinnode.model.Message;

import com.boetcoin.bitcoinnode.util.Util;

import java.math.BigInteger;
import java.util.ArrayList;

/**
 * Created by rossbadenhorst on 2018/02/05.
 */

public abstract class BaseMessage {

    public static long PACKET_MAGIC_MAINNET = 0xf9beb4d9L;

    /**
     * The max size of a message according to the BTC protocol.
     */
    public static int MAX_SIZE = 33554432;

    /**
     * Length of the magic bytes in header.
     * In bytes.
     */
    public static final int HEADER_LENGTH_MAGIC_BYTES = 4;
    /**
     * Length of the command in header.
     * In bytes.
     */
    public static final int HEADER_LENGTH_COMMAND = 12;
    /**
     * Length of the payload size in header.
     * In bytes.
     */
    public static final int HEADER_LENGTH_PAYLOAD_SIZE = 4;
    /**
     * Length of the checksum in header.
     * In bytes.
     */
    public static final int HEADER_LENGTH_CHECKSUM = 4;

    /**
     * The header of the message that has been sent or received.
     */
    protected byte[] header;
    /**
     * The payload of the message that has been sent or received.
     */
    protected byte[] payload;
    /**
     * Used for when we build up a payload to send out to a peer
     */
    protected ArrayList<Integer> outputPayload;
    /**
     * How many bytes into the payload does the good stuff start at.
     * Not to sure about this, but bitcoinj had this stuff... and they are clever as fuck!
     */
    protected int offset;
    /**
     * This keeps track of where we are in the payload.
     * As we read strings and intergers and desifer the payload, we increment the cursor.
     *
     * That way once we have read one element, the cursor will be in the right place
     * to start reading the next element... make sense?
     */
    protected int cursor;

    public BaseMessage() {
        outputPayload = new ArrayList<>();
    }

    public BaseMessage(byte[] header, byte[] payload) {
        this.header = header;
        this.payload = payload;

        readPayload();
    }

    public byte[] getHeader() {
        return this.header;
    }

    public byte[] getPayload() {
        return this.payload;
    }

    protected void writeHeader() {
        header = new byte[HEADER_LENGTH_MAGIC_BYTES + HEADER_LENGTH_COMMAND + HEADER_LENGTH_PAYLOAD_SIZE + HEADER_LENGTH_CHECKSUM];

        Util.addToByteArray(BaseMessage.PACKET_MAGIC_MAINNET, 0, BaseMessage.HEADER_LENGTH_MAGIC_BYTES, header);
        Util.addToByteArray(getCommandName(), HEADER_LENGTH_MAGIC_BYTES, HEADER_LENGTH_COMMAND, header);
        Util.addToByteArray(this.payload.length, HEADER_LENGTH_MAGIC_BYTES + HEADER_LENGTH_COMMAND, HEADER_LENGTH_PAYLOAD_SIZE, header);
        Util.addToByteArray(Util.doubleDigest(this.payload), HEADER_LENGTH_MAGIC_BYTES + HEADER_LENGTH_COMMAND + HEADER_LENGTH_PAYLOAD_SIZE , HEADER_LENGTH_CHECKSUM, header);
    }

    /**
     * Takes the payload and tries to make sense of it!
     *
     * We will use methods like readStr() or readInt()
     * To dissect the payload byte array.
     *
     * This is where we adjust the cursor and "read" the payload array sequentially.
     */
    protected abstract void readPayload();

    public abstract String getCommandName();

    protected abstract void writePayload();

    /**
     * Reads a string from the payload byte array.
     * The length of the string is the prepended by its length.
     *
     * We need to get that first, so we know how long the string is that we want to read.
     * @return a nice little string that is in the payload.
     */
    protected String readStr() {
        long length = readVarInt();
        return length == 0 ? "" : Util.toString(readBytes((int) length), "UTF-8"); // optimization for empty strings
    }

    /**
     * Reads a variable-length unsigned integer from the payload.
     * I believe this is used Satoshi's unique encoding style.
     *
     * This will adjust the cursor once we have read it,
     * so we know where the next element to read starts.
     *
     * @return the int.
     */
    protected long readVarInt() {
        long value;
        int originallyEncodedSize;

        int first = 0xFF & payload[cursor];
        if (first < 253) {
            value = first;
            originallyEncodedSize = 1; // 1 data byte (8 bits)
        } else if (first == 253) {
            value = (0xFF & payload[cursor + 1]) | ((0xFF & payload[cursor + 2]) << 8);
            originallyEncodedSize = 3; // 1 marker + 2 data bytes (16 bits)
        } else if (first == 254) {
            value = Util.readUint32(payload, cursor + 1);
            originallyEncodedSize = 5; // 1 marker + 4 data bytes (32 bits)
        } else {
            value = Util.readInt64(payload, cursor + 1);
            originallyEncodedSize = 9; // 1 marker + 8 data bytes (64 bits)
        }

        cursor += originallyEncodedSize;
        return value;
    }

    /**
     * Gets a nice byte array from the payload.
     *
     * This will adjust the cursor once we have read it,
     * so we know where the next element to read starts.
     *
     * @param length - length of the byte array to dig out the payload.
     * @return smaller byte array from the payload.
     */
    protected byte[] readBytes(int length) {
        try {
            byte[] b = new byte[length];
            System.arraycopy(payload, cursor, b, 0, length);
            cursor += length;
            return b;
        } catch (IndexOutOfBoundsException e) {
            cursor += length;
            return new byte[length];
        }
    }

    protected long readUint32() {
        long u = Util.readUint32(payload, cursor);
        cursor += 4;
        return u;
    }

    protected BigInteger readUint64() {
        // Java does not have an unsigned 64 bit type. So scrape it off the wire then flip.
        return new BigInteger(Util.reverseBytes(readBytes(8)));
    }

    protected void writeVarInt(long value) {
        switch (Util.sizeOf(value)) {
            case 1:
                outputPayload.add((int) value);
                break;
            case 3:
                outputPayload.add((253));
                outputPayload.add((int) value);
                outputPayload.add((int) value >> 8);
                break;
            case 5:
                writeInt(254);
                writeUint32(value);
                break;
            default:
                writeInt(255);
                writeUint64(value);
        }
    }

    protected void writeUint32(long value) {
        outputPayload.add((int) (0xFF & value));
        outputPayload.add((int) (0xFF & (value >> 8)));
        outputPayload.add((int) (0xFF & (value >> 16)));
        outputPayload.add((int) (0xFF & (value >> 24)));
    }

    protected void writeUint64(long value) {
        outputPayload.add((int) (0xFF & value));
        outputPayload.add((int) (0xFF & (value >> 8)));
        outputPayload.add((int) (0xFF & (value >> 16)));
        outputPayload.add((int) (0xFF & (value >> 24)));
        outputPayload.add((int) (0xFF & (value >> 32)));
        outputPayload.add((int) (0xFF & (value >> 40)));
        outputPayload.add((int) (0xFF & (value >> 48)));
        outputPayload.add((int) (0xFF & (value >> 56)));
    }

    protected void writeUint64(BigInteger value) {
        byte[] bytes = value.toByteArray();
        if (bytes.length > 8) {
            throw new RuntimeException("Input too large to encode into a uint64");
        }
        bytes = Util.reverseBytes(bytes);
        for (byte b : bytes) {
            outputPayload.add((int) b);
        }
        if (bytes.length < 8) {
            for (int i = 0; i < 8 - bytes.length; i++)
                outputPayload.add(0);
        }
    }

    protected void writeInt(int value) {
        outputPayload.add(value);
    }

    protected void writeBytes(byte[] values) {
        for(byte value : values) {
            outputPayload.add((int) value);
        }
    }
}
