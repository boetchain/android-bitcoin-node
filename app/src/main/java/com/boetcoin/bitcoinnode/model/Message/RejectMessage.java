package com.boetcoin.bitcoinnode.model.Message;

import android.util.Log;

import com.boetcoin.bitcoinnode.App;
import com.boetcoin.bitcoinnode.util.Util;

import java.io.UnsupportedEncodingException;

/**
 * Created by rossbadenhorst on 2018/02/03.
 */

public class RejectMessage extends BaseMessage {

    /**
     * The type of message being rejected
     */
    String message;
    /**
     * The code relating to the rejected message
     */
    byte ccode;
    /**
     * Text version of the reason for rejection
     */
    String reason;
    /**
     * Optional data.
     */
    char data;

    public static byte MALFORMED        = 0x01;
    public static byte INVALID          = 0x10;
    public static byte OBSOLETE         = 0x11;
    public static byte DUPLICATE        = 0x12;
    public static byte NONSTANDARD      = 0x40;
    public static byte DUST             = 0x41;
    public static byte INSUFFICIENTFEE  = 0x42;
    public static byte CHECKPOINT       = 0x43;
    public static int OTHER             = 0xff;

    protected int offset;
    protected int cursor;

    /**
     * The command name of the reject message as defined in the protocol.
     */
    public static final String COMMAND_NAME = "reject";

    public RejectMessage(byte[] byteHeader, byte[] bytePayload) {
        this.byteHeader     = byteHeader;
        this.bytePayload    = bytePayload;

        //this.message = readStr();
        //this.ccode = getCode(readBytes(1)[0]);
        //this.reason = readStr();


        Log.i(App.TAG, "MSG: " + this.message);
        Log.i(App.TAG, "reason: " + this.reason);
    }

    @Override
    public String getCommandName() {
        return COMMAND_NAME;
    }

    @Override
    int getPayloadSize() {
        return 0;
    }

    private byte getCode(byte code) {
        if (code == MALFORMED) {
            Log.i(App.TAG, "code == MALFORMED");
            return  MALFORMED;
        }

        if (code == INVALID) {
            Log.i(App.TAG, "code == INVALID");
            return  INVALID;
        }

        if (code == MALFORMED) {
            Log.i(App.TAG, "code == MALFORMED");
            return  MALFORMED;
        }

        if (code == OBSOLETE) {
            Log.i(App.TAG, "code == OBSOLETE");
            return  OBSOLETE;
        }

        if (code == DUPLICATE) {
            Log.i(App.TAG, "code == DUPLICATE");
            return  DUPLICATE;
        }

        if (code == NONSTANDARD) {
            Log.i(App.TAG, "code == NONSTANDARD");
            return  NONSTANDARD;
        }

        if (code == DUST) {
            Log.i(App.TAG, "code == DUST");
            return  DUST;
        }
        if (code == INSUFFICIENTFEE) {
            Log.i(App.TAG, "code == INSUFFICIENTFEE");
            return  INSUFFICIENTFEE;
        }
        if (code == CHECKPOINT) {
            Log.i(App.TAG, "code == CHECKPOINT");
            return  CHECKPOINT;
        }

        Log.i(App.TAG, "code == OTHER");
        return (byte) OTHER;
    }



    protected long readVarInt() {
        return readVarInt(0);
    }

    protected long readVarInt(int offset) {
        try {
            VarInt varint = new VarInt(bytePayload, cursor + offset);
            cursor += offset + varint.getOriginalSizeInBytes();
            return varint.value;
        } catch (ArrayIndexOutOfBoundsException e) {
            return 0;
        }
    }

    protected byte[] readBytes(int length) {
        Log.i(App.TAG, "readBytes: " + length);
        try {
            byte[] b = new byte[length];
            System.arraycopy(bytePayload, cursor, b, 0, length);
            cursor += length;
            return b;
        } catch (IndexOutOfBoundsException e) {
            return new byte[0];
        }
    }


    class VarInt {
        public final long value;
        private final int originallyEncodedSize;

        /**
         * Constructs a new VarInt with the given unsigned long value.
         *
         * @param value the unsigned long value (beware widening conversion of negatives!)
         */
        public VarInt(long value) {
            this.value = value;
            originallyEncodedSize = getSizeInBytes();
        }

        /**
         * Constructs a new VarInt with the value parsed from the specified offset of the given buffer.
         *
         * @param buf    the buffer containing the value
         * @param offset the offset of the value
         */
        public VarInt(byte[] buf, int offset) {
            int first = 0xFF & buf[offset];
            if (first < 253) {
                value = first;
                originallyEncodedSize = 1; // 1 data byte (8 bits)
            } else if (first == 253) {
                value = (0xFF & buf[offset + 1]) | ((0xFF & buf[offset + 2]) << 8);
                originallyEncodedSize = 3; // 1 marker + 2 data bytes (16 bits)
            } else if (first == 254) {
                value = Util.readUint32(buf, offset + 1);
                originallyEncodedSize = 5; // 1 marker + 4 data bytes (32 bits)
            } else {
                value = Util.readInt64(buf, offset + 1);
                originallyEncodedSize = 9; // 1 marker + 8 data bytes (64 bits)
            }
        }

        /**
         * Returns the original number of bytes used to encode the value if it was
         * deserialized from a byte array, or the minimum encoded size if it was not.
         */
        public int getOriginalSizeInBytes() {
            return originallyEncodedSize;
        }

        /**
         * Returns the minimum encoded size of the value.
         */
        public final int getSizeInBytes() {
            return sizeOf(value);
        }

        /**
         * Returns the minimum encoded size of the given unsigned long value.
         *
         * @param value the unsigned long value (beware widening conversion of negatives!)
         */
        public int sizeOf(long value) {
            // if negative, it's actually a very large unsigned long value
            if (value < 0) return 9; // 1 marker + 8 data bytes
            if (value < 253) return 1; // 1 data byte
            if (value <= 0xFFFFL) return 3; // 1 marker + 2 data bytes
            if (value <= 0xFFFFFFFFL) return 5; // 1 marker + 4 data bytes
            return 9; // 1 marker + 8 data bytes
        }

        /**
         * Encodes the value into its minimal representation.
         *
         * @return the minimal encoded bytes of the value
         */
        public byte[] encode() {
            byte[] bytes;
            switch (sizeOf(value)) {
                case 1:
                    return new byte[]{(byte) value};
                case 3:
                    return new byte[]{(byte) 253, (byte) (value), (byte) (value >> 8)};
                case 5:
                    bytes = new byte[5];
                    bytes[0] = (byte) 254;
                    Util.uint32ToByteArrayLE(value, bytes, 1);
                    return bytes;
                default:
                    bytes = new byte[9];
                    bytes[0] = (byte) 255;
                    Util.uint64ToByteArrayLE(value, bytes, 1);
                    return bytes;
            }
        }
    }
}
