package com.boetcoin.bitcoinnode.model.Msg;

import android.util.Log;

import com.boetcoin.bitcoinnode.App;
import com.boetcoin.bitcoinnode.util.Util;

/**
 * Created by rossbadenhorst on 2018/02/05.
 */

public abstract class BaseMsg {

    /**
     * The header of the message that has been sent or received.
     */
    protected byte[] header;
    /**
     * The payload of the message that has been sent or received.
     */
    protected byte[] payload;
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

    public BaseMsg(byte[] header, byte[] payload) {
        this.header = header;
        this.payload = payload;

        parse();
    }

    /**
     * Takes the payload and tries to make sense of it!
     *
     * We will use methods like readStr() or readInt()
     * To dissect the payload byte array.
     *
     * This is where we adjust the cursor and "read" the array sequentially.
     */
    protected abstract void parse();

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
}
