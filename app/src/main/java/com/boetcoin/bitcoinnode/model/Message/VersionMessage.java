package com.boetcoin.bitcoinnode.model.Message;

import com.boetcoin.bitcoinnode.util.Util;

/**
 * Created by rossbadenhorst on 2018/02/05.
 */

public class VersionMessage extends BaseMessage {

    public static final String COMMAND_NAME = "version";

    /**
     * Identifies protocol version being used by the node
     */
    private int version;
    /**
     * Bitfield of features to be enabled for this connection.
     */
    private long services;
    /**
     * standard UNIX timestamp in seconds.
     */
    private long timestamp;
    /**
     * The network address of the node receiving this message
     */
    private String addrRecv;
    /**
     * The network address of the node emitting this message.
     */
    private String addrFrom;
    /**
     * Node random nonce, randomly generated every time a version packet is sent.
     * This nonce is used to detect connections to self.
     */
    private long nonce;
    /**
     * Client name :?
     */
    private String userAgent;
    /**
     * The last block received by the emitting node
     */
    private int startHeight;
    /**
     * Whether the remote peer should announce relayed transactions or not.
     */
    private boolean relay;

    public VersionMessage() {
        super();
        this.version = 70012;
        this.services = 505;
        this.timestamp = System.currentTimeMillis() / 1000;
        this.addrRecv = "";
        this.addrFrom = "";
        this.nonce = 22;
        this.userAgent = "BoetChain";
        this.startHeight = 200;
        this.relay = true;

        writePayload();
        writeHeader();
    }

    public VersionMessage(byte[] header, byte[] payload) {
        super(header, payload);
    }

    @Override
    protected void writePayload() {

        writeUint32(this.version);
        writeUint64(this.services);
        writeUint64(this.timestamp);
        writeAddress();// addr receive - not used :?
        writeAddress();// addr from - not used :?
        writeUint64(this.nonce);
        writeStr(userAgent);
        writeUint32(startHeight);
        writeInt(relay ? 1 : 0);

        payload = new byte[outputPayload.size()];
        for (int i = 0; i < payload.length && i < outputPayload.size(); i++) {
            payload[i] = outputPayload.get(i).byteValue();
        }
    }

    private void writeAddress() {
        byte[] dummyAddr = new byte[26];
        Util.addToByteArray("$234567890123456789055555*", 0, 26, dummyAddr);
        writeBytes(dummyAddr);
    }

    protected String readAddress() {
        return Util.toString(readBytes((int) 26), "UTF-8");
    }

    @Override
    protected void readPayload() {
        this.version = (int) readUint32();
        this.services = readUint64().longValue();
        this.timestamp = readUint64().longValue();
        this.addrRecv = readAddress();
        this.addrFrom = readAddress();
        this.nonce = readUint64().longValue();
        this.userAgent = readStr();
        this.startHeight = (int) readUint32();
        this.relay = readBytes(1)[0] != 0;
    }

    @Override
    public String getCommandName() {
        return COMMAND_NAME;
    }

    @Override
    public String toString() {
        return "VersionMessage{" +
                "version=" + version +
                ", services=" + services +
                ", timestamp=" + timestamp +
                ", addrRecv='" + addrRecv + '\'' +
                ", addrFrom='" + addrFrom + '\'' +
                ", nonce=" + nonce +
                ", userAgent='" + userAgent + '\'' +
                ", startHeight=" + startHeight +
                ", relay=" + relay +
                '}';
    }
}
