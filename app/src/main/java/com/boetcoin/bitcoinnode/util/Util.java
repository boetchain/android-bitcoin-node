package com.boetcoin.bitcoinnode.util;

import com.google.common.primitives.Longs;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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

}
