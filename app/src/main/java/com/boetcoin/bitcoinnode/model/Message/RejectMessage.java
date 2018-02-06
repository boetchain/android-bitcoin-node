package com.boetcoin.bitcoinnode.model.Message;

/**
 * Created by rossbadenhorst on 2018/02/05.
 */

public class RejectMessage extends BaseMessage {

    public static final String COMMAND_NAME = "reject";

    /**
     * The type of message that is rejected.
     */
    private String message;
    /**
     * The code the rejection occurred.
     */
    private int code;
    /**
     * Text version for the reason of rejections.
     */
    private String reason;

    public static byte REJECT_MALFORMED        = 0x01;
    public static byte REJECT_INVALID          = 0x10;
    public static byte REJECT_OBSOLETE         = 0x11;
    public static byte REJECT_DUPLICATE        = 0x12;
    public static byte REJECT_NONSTANDARD      = 0x40;
    public static byte REJECT_DUST             = 0x41;
    public static byte REJECT_INSUFFICIENTFEE  = 0x42;
    public static byte REJECT_CHECKPOINT       = 0x43;
    public static int REJECT_OTHER             = 0xff;

    @Override
    protected void writePayload() {
    }

    public RejectMessage(byte[] header, byte[] payload) {
        super(header, payload);
    }

    @Override
    protected void readPayload() {
        this.message = readStr();
        this.code = getCode(readBytes(1)[0]);
        this.reason = readStr();
    }

    /**
     * Gets the code of the reason for rejection.
     * If we dont know what code it is, we assume it is OTHER :?
     * @param code - reason for rejection.
     * @return REJECT_*
     */
    private byte getCode(byte code) {
        if (code == REJECT_MALFORMED) {
            return  REJECT_MALFORMED;
        }
        if (code == REJECT_INVALID) {
            return  REJECT_INVALID;
        }
        if (code == REJECT_MALFORMED) {
            return  REJECT_MALFORMED;
        }
        if (code == REJECT_OBSOLETE) {
            return  REJECT_OBSOLETE;
        }
        if (code == REJECT_DUPLICATE) {
            return  REJECT_DUPLICATE;
        }
        if (code == REJECT_NONSTANDARD) {
            return  REJECT_NONSTANDARD;
        }
        if (code == REJECT_DUST) {
            return  REJECT_DUST;
        }
        if (code == REJECT_INSUFFICIENTFEE) {
            return  REJECT_INSUFFICIENTFEE;
        }
        if (code == REJECT_CHECKPOINT) {
            return  REJECT_CHECKPOINT;
        }

        return (byte) REJECT_OTHER;
    }

    @Override
    public String getCommandName() {
        return COMMAND_NAME;
    }
    @Override
    public String toString() {
        return "Reject: " + message + ", " + reason;
    }
}
