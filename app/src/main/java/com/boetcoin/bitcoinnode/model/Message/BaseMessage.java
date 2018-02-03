package com.boetcoin.bitcoinnode.model.Message;

import android.util.Log;

import com.boetcoin.bitcoinnode.App;
import com.boetcoin.bitcoinnode.util.Util;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rossbadenhorst on 2018/02/01.
 */

public abstract class BaseMessage {

    public static long PACKET_MAGIC_MAINNET = 0xf9beb4d9L;

    /**
     * The max size of a message according to the BTC protocol.
     */
    public static int MAX_SIZE = 33554432;

    /**
     * The length of the magic bytes in the header.
     * In bytes.
     */
    public static final int HEADER_MAGIC_STRING_LENGTH = 4;
    /**
     * The length or size of the command in the header.
     * In bytes.
     */
    public static final int HEADER_COMMAND_LENGTH = 12;
    /**
     * The length or size of the payload in the header.
     * In bytes.
     */
    public static final int HEADER_PAYLOAD_SIZE_LENGTH = 4;
    /**
     * The length or size of the checksum in the header
     * In bytes.
     */
    public static final int HEADER_CHECKSUM_LENGTH = 4;

    /**
     * The header of the message that has been sent or received.
     */
    protected List<MessageItem> header;
    /**
     * The payload of the message that has been sent or received.
     */
    protected List<MessageItem> payload;

    protected byte[] byteHeader;
    protected byte[] bytePayload;

    public BaseMessage() {
        initPayload();
        initHeader();
    }

    /**
     * Creates the header according to the bitcoin protocol.
     */
    protected void initHeader() {
        header = new ArrayList<>();
        header.add(new MessageItem(PACKET_MAGIC_MAINNET,    HEADER_MAGIC_STRING_LENGTH));
        header.add(new MessageItem(getCommandName(),        HEADER_COMMAND_LENGTH));
        header.add(new MessageItem(getPayloadSize(),        HEADER_PAYLOAD_SIZE_LENGTH));
        header.add(new MessageItem(getCheckSum(),           HEADER_CHECKSUM_LENGTH));
    }

    /**
     * Creates the payload according to the bitcoin protocol.
     */
    protected void initPayload() {
        payload = new ArrayList<>();
    }

    /**
     * Gets the name of the message (called the command name)
     * @return - type or name of the message
     */
    public abstract String getCommandName();

    /**
     * Gets the header in byte array format.
     * Used when we actually want to transmit the message
     * @return - byte array of the header.
     */
    public byte[] getHeader() {
        byte[] bytes = new byte[getHeaderSize()];

        int cursor = 0;
        for (MessageItem item : header) {
            byte[] itemArray = item.value;

            for (int i = 0; i < itemArray.length; i ++) {
                bytes[cursor]= itemArray[i];
                cursor++;
            }
        }

        return bytes;
    }

    /**
     * Gets the payload in byte array format.
     * Used when we actually wan to transmit the message.
     * @return - byte array of the payload.
     */
    public byte[] getPayload() {
        byte[] bytes = new byte[getPayloadSize()];

        int cursor = 0;
        for (MessageItem item : payload) {
            byte[] itemArray = item.value;

            for (int i = 0; i < itemArray.length; i ++) {
                bytes[cursor]= itemArray[i];
                cursor++;
            }
        }

        return bytes;
    }

    /**
     * Gets the length of the header in bytes.
     * @return - how many bytes does the header contain
     */
    public int getHeaderSize() {
        int headerSizeInBytes = 0;

        for (MessageItem messageItem : header) {
            headerSizeInBytes += messageItem.value.length;
        }

        return headerSizeInBytes;
    }

    /**
     * Gets the length of the payload in bytes.
     * @return - how many bytes does the payload contain
     */
    abstract int getPayloadSize();

    /**
     * @return - the check sum of the payload.
     */
    public byte[] getCheckSum() {
        return Util.doubleDigest(getPayload());
    }

    @Override
    public String toString() {
        String output = "Message Header: \n";

        for (MessageItem messageItem : header) {
            output += Util.formatHexString(Util.bytesToHexString(messageItem.value)) + " \n";
        }

        output += "\n" + getCommandName() + " Payload: \n";

        for (MessageItem messageItem : payload) {
            output += Util.formatHexString(Util.bytesToHexString(messageItem.value)) + " \n";
        }

        return output;
    }

    protected class MessageItem {
        public byte[] value;
        public int maxlength;

        public MessageItem(byte[] value, int maxlength) {
            this.value = new byte[maxlength];
            for (int i = 0; i < maxlength && i < value.length && i < this.value.length; i++) {
                this.value[i] = value[i];
            }
        }

        public MessageItem(long value, int maxlength) {
            this.value = new byte[maxlength];
            Util.addToByteArray(value, 0, maxlength, this.value);
        }

        public MessageItem(String value, int maxlength) {
            this.value = new byte[maxlength];
            Util.addToByteArray(value, 0, maxlength, this.value);
        }

        public MessageItem(boolean value, int maxlength) {
            this.value = new byte[maxlength];
            Util.addToByteArray(value, 0, maxlength, this.value);
        }
    }
}
