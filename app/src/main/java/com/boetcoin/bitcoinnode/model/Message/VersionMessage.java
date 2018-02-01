package com.boetcoin.bitcoinnode.model.Message;

import java.util.UUID;

/**
 * Created by rossbadenhorst on 2018/02/01.
 */

public class VersionMessage extends BaseMessage {

    /**
     * The command name of the version message as defined in the protocol.
     */
     public static final String COMMAND_NAME = "version";
    /**
     * The length or size of the version in the payload.
     * In bytes.
     */
    private static final int PAYLOAD_VERSION_LENGTH = 4;
    /**
     * The length or size of the services in the payload.
     * In bytes.
     */
    private static final int PAYLOAD_SERVICES_LENGTH = 8;
    /**
     * The length or size of the timestamp in the payload.
     * In bytes.
     */
    private static final int PAYLOAD_TIMESTAMP_LENGTH = 8;
    /**
     * The length or size of the address of the recipient in the payload.
     * In bytes.
     */
    private static final int PAYLOAD_RECIPIENT_ADDR_LENGTH = 26;
    /**
     * The length or size of the address of the sender in the payload.
     * In bytes.
     */
    private static final int PAYLOAD_SENDER_ADDR_LENGTH = 26;
    /**
     * The length or size of the nonce in the payload.
     * In bytes.
     */
    private static final int PAYLOAD_NONCE_LENGTH = 8;
    /**
     * The length or size of the user agent in the payload.
     * In bytes.
     */
    private static final int PAYLOAD_USER_AGENT_LENGTH = 8;
    /**
     * The length or size of the block height in the payload.
     * In bytes.
     */
    private static final int PAYLOAD_HEIGHT_LENGTH = 8;
    /**
     * The length or size of the relay in the payload
     */
    private static final int PAYLOAD_RELAY_LENGTH = 1;

    /**
     * The version of the protocol we wish to use.
     */
    public static final int VERSION = 31900;
    /**
     * The signal to say have full blocks, not just headers.
     */
    public static final int NODE_NETWORK = 1;
    /**
     * Who we are!
     */
    public static final String USER_AGENT = "boetchain";
    /**
     * The height of the block chain we have locally.
     */
    public static final int blockheight = 0;
    /**
     * If we want peeps to send us block updates.
     */
    public static final boolean relay = false;

    @Override
    public String getCommandName() {
        return COMMAND_NAME;
    }

    @Override
    int getPayloadSize() {
        int payloadSizeInBytes = 0;

        for (MessageItem messageItem : payload) {
            payloadSizeInBytes += messageItem.value.length;
        }

        return payloadSizeInBytes;
    }

    @Override
    long getCheckSum() {
        return 0;
    }

    @Override
    protected void initPayload() {
        super.initPayload();

        payload.add(new MessageItem(VERSION,                                    PAYLOAD_VERSION_LENGTH));
        payload.add(new MessageItem(NODE_NETWORK,                               PAYLOAD_SERVICES_LENGTH));
        payload.add(new MessageItem(System.currentTimeMillis() / 1000,    PAYLOAD_TIMESTAMP_LENGTH));
        payload.add(new MessageItem("RECIPIENT IP",                       PAYLOAD_RECIPIENT_ADDR_LENGTH));
        payload.add(new MessageItem("SENDER IP",                          PAYLOAD_SENDER_ADDR_LENGTH));
        payload.add(new MessageItem(UUID.randomUUID().toString(),               PAYLOAD_NONCE_LENGTH));
        payload.add(new MessageItem(USER_AGENT,                                 PAYLOAD_USER_AGENT_LENGTH));
        payload.add(new MessageItem(blockheight,                                PAYLOAD_HEIGHT_LENGTH));
        payload.add(new MessageItem(relay,                                      PAYLOAD_RELAY_LENGTH));
    }
}
