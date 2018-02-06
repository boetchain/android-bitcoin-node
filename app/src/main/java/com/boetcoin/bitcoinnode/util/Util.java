package com.boetcoin.bitcoinnode.util;

import android.util.Log;

import com.boetcoin.bitcoinnode.App;
import com.google.common.primitives.Longs;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 * Created by rossbadenhorst on 2018/02/01.
 */

public class Util {

    /**
     * Converts bytes to a hex string.
     * Often used to debug messages and any other bytes stuffs.
     *
     * @param bytes - bytes to converts.
     * @return Hex String representation of the bytes.
     */
    public static String bytesToHexString(byte[] bytes) {
        StringBuffer buf = new StringBuffer(bytes.length * 2);
        for (byte b : bytes) {
            String s = Integer.toString(0xFF & b, 16);
            if (s.length() < 2)
                buf.append('0');
            buf.append(s);
        }
        return buf.toString();
    }

    public static String byteToHexString(byte value) {
        StringBuffer buf = new StringBuffer(2);
        String s = Integer.toString(0xFF & value, 16);
        if (s.length() < 2)
            buf.append('0');
        buf.append(s);
        return buf.toString();
    }

    /**
     * See {@link Utils#doubleDigest(byte[],int,int)}.
     */
    public static byte[] doubleDigest(byte[] input) {
        return doubleDigest(input, 0, input.length);
    }

    /**
     * Calculates the SHA-256 hash of the given byte range, and then hashes the resulting hash again. This is
     * standard procedure in BitCoin. The resulting hash is in big endian form.
     */
    public static byte[] doubleDigest(byte[] input, int offset, int length) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(input, offset, length);
            byte[] first = digest.digest();
            return digest.digest(first);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);  // Cannot happen.
        }
    }

    public static String formatHexString(String hexString) {
        String formattedHexString = "";
        for (int i = 0; i < hexString.length(); i+= 2) {
            formattedHexString += hexString.substring(i, i + 2) + " ";
        }
        return formattedHexString;
    }

    public static void addToByteArray(String valueToAdd, int startPosition, int maxValueLength, byte[] arrayToAdd) {
        byte[] valueArray = valueToAdd.getBytes();
        addToByteArray(valueArray, startPosition, maxValueLength, arrayToAdd);
    }

    public static void addToByteArray(int valueToAdd, int startPosition, int maxValueLength, byte[] arrayToAdd) {
        byte[] valueArray  = ByteBuffer.allocate(4).putInt(valueToAdd).array();
        addToByteArray(valueArray, startPosition, maxValueLength, arrayToAdd);
    }

    public static void addToByteArray(long valueToAdd, int startPosition, int maxValueLength, byte[] arrayToAdd) {
        byte[] valueArray = Longs.toByteArray(valueToAdd);
        addToByteArray(valueArray, startPosition, maxValueLength, arrayToAdd);
    }

    public static void addToByteArray(boolean valueToAdd, int startPosition, int maxValueLength, byte[] arrayToAdd) {
        byte[] valueArray = new byte[1];
        if (valueToAdd) {
            valueArray[0] = 1;
        } else {
            valueArray[0] = 0;
        }
        addToByteArray(valueArray, startPosition, maxValueLength, arrayToAdd);
    }

    public static void addToByteArray(byte[] valueArray, int startPosition, int maxValueLength, byte[] arrayToAdd) {
        int numberOfBytesAdded = 0;
        for (int i = 0; i < valueArray.length && numberOfBytesAdded < maxValueLength; i++) {
            if (valueArray[i] != 0) {
                arrayToAdd[startPosition + numberOfBytesAdded] = valueArray[i];
                numberOfBytesAdded++;
            }
        }
    }

    public static byte[] toByteArray(List<Byte> in) {
        final int n = in.size();
        byte ret[] = new byte[n];
        for (int i = 0; i < n; i++) {
            ret[i] = in.get(i);
        }
        return ret;
    }

    public static long readUint32(byte[] bytes, int offset) {
        return ((bytes[offset++] & 0xFFL) <<  0) |
                ((bytes[offset++] & 0xFFL) <<  8) |
                ((bytes[offset++] & 0xFFL) << 16) |
                ((bytes[offset] & 0xFFL) << 24);
    }

    /** Parse 8 bytes from the byte array (starting at the offset) as signed 64-bit integer in little endian format. */
    public static long readInt64(byte[] bytes, int offset) {
        return (bytes[offset] & 0xffl) |
                ((bytes[offset + 1] & 0xffl) << 8) |
                ((bytes[offset + 2] & 0xffl) << 16) |
                ((bytes[offset + 3] & 0xffl) << 24) |
                ((bytes[offset + 4] & 0xffl) << 32) |
                ((bytes[offset + 5] & 0xffl) << 40) |
                ((bytes[offset + 6] & 0xffl) << 48) |
                ((bytes[offset + 7] & 0xffl) << 56);
    }

    public static void uint32ToByteArrayLE(long val, byte[] out, int offset) {
        out[offset] = (byte) (0xFF & val);
        out[offset + 1] = (byte) (0xFF & (val >> 8));
        out[offset + 2] = (byte) (0xFF & (val >> 16));
        out[offset + 3] = (byte) (0xFF & (val >> 24));
    }

    public static void uint64ToByteArrayLE(long val, byte[] out, int offset) {
        out[offset] = (byte) (0xFF & val);
        out[offset + 1] = (byte) (0xFF & (val >> 8));
        out[offset + 2] = (byte) (0xFF & (val >> 16));
        out[offset + 3] = (byte) (0xFF & (val >> 24));
        out[offset + 4] = (byte) (0xFF & (val >> 32));
        out[offset + 5] = (byte) (0xFF & (val >> 40));
        out[offset + 6] = (byte) (0xFF & (val >> 48));
        out[offset + 7] = (byte) (0xFF & (val >> 56));
    }

    /**
     * Returns a copy of the given byte array in reverse order.
     */
    public static byte[] reverseBytes(byte[] bytes) {
        byte[] buf = new byte[bytes.length];
        for (int i = 0; i < bytes.length; i++)
            buf[i] = bytes[bytes.length - 1 - i];
        return buf;
    }

    /**
     * Returns the minimum encoded size of the given unsigned long value.
     *
     * @param value the unsigned long value (beware widening conversion of negatives!)
     */
    public static int sizeOf(long value) {
        // if negative, it's actually a very large unsigned long value
        if (value < 0) return 9; // 1 marker + 8 data bytes
        if (value < 253) return 1; // 1 data byte
        if (value <= 0xFFFFL) return 3; // 1 marker + 2 data bytes
        if (value <= 0xFFFFFFFFL) return 5; // 1 marker + 4 data bytes
        return 9; // 1 marker + 8 data bytes
    }

    /**
     * Constructs a new String by decoding the given bytes using the specified charset.
     * <p>
     * This is a convenience method which wraps the checked exception with a RuntimeException.
     * The exception can never occur given the charsets
     * US-ASCII, ISO-8859-1, UTF-8, UTF-16, UTF-16LE or UTF-16BE.
     *
     * @param bytes the bytes to be decoded into characters
     * @param charsetName the name of a supported {@linkplain java.nio.charset.Charset charset}
     * @return the decoded String
     */
    public static String toString(byte[] bytes, String charsetName) {
        try {
            return new String(bytes, charsetName);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

}
