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

        Log.i(App.TAG, "payload: " + bytePayload.length);
        this.message = getMessage(bytePayload);
    }

    private String getMessage(byte[] payload) {
        Log.i(App.TAG, "getMessage...");
        String message = "";

        int len = 9999999;

        byte[] commandNameByteArray = new byte[len];
        for (int i = 0; i < len && i < payload.length; i++) {
            commandNameByteArray[i] = payload[i];
        }

        try {
            message = new String(commandNameByteArray, "US-ASCII");
        } catch (UnsupportedEncodingException e) {
            return "nothing found?";
        }

        Log.i(App.TAG, "message: " + message);

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
