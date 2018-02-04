package com.boetcoin.bitcoinnode.model.Message;

import android.util.Log;

import com.boetcoin.bitcoinnode.App;

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
    char ccode;
    /**
     * Text version of the reason for rejection
     */
    String reason;
    /**
     * Optional data.
     */
    char data;

    /**
     * The command name of the reject message as defined in the protocol.
     */
    public static final String COMMAND_NAME = "reject";

    public RejectMessage(byte[] byteHeader, byte[] bytePayload) {
        this.byteHeader     = byteHeader;
        this.bytePayload    = bytePayload;
        this.message = getMessage(bytePayload);
    }

    /**
     * Takes the whole payload and makes it into a String
     * @param payload
     * @return
     */
    private String getMessage(byte[] payload) {

        try {
            message = new String(payload, "US-ASCII");
        } catch (UnsupportedEncodingException e) {
            return "nothing found?";
        }

        return message;
    }

    @Override
    public String getCommandName() {
        return COMMAND_NAME;
    }

    @Override
    int getPayloadSize() {
        return 0;
    }
}
