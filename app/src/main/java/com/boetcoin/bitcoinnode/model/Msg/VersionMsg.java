package com.boetcoin.bitcoinnode.model.Msg;

import android.util.Log;

import com.boetcoin.bitcoinnode.App;

import java.math.BigInteger;

/**
 * Created by rossbadenhorst on 2018/02/05.
 */

public class VersionMsg extends BaseMsg {

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

    public VersionMsg() {
        super();
        this.version = 70012;
        this.services = 0;
        this.timestamp = System.currentTimeMillis() / 1000;
        this.addrRecv = "";
        this.addrFrom = "";
        this.nonce = 1;
        this.userAgent = "BoetChain";
        this.startHeight = 0;

        writePayload();
        writeHeader();
    }

    public VersionMsg(byte[] header, byte[] payload) {
        super(header, payload);
    }

    @Override
    protected void writePayload() {

        writeUint32(this.version);
        writeUint32(this.services);
        writeUint32(this.services >> 32);
        writeUint32(this.timestamp);
        writeUint32(this.timestamp >> 32);
        writeAddress();// addr receive - not used :?
        writeAddress();// addr from - not used :?
        writeUint32(nonce);
        writeVarInt(userAgent.getBytes().length);
        writeBytes(userAgent.getBytes());
        writeUint32(startHeight);
        writeInt(relay ? 1 : 0);




        payload = new byte[outputPayload.size()];
        for (int i = 0; i < payload.length && i < outputPayload.size(); i++) {
            payload[i] = outputPayload.get(i).byteValue();
        }
    }

    private void writeAddress() {
        BigInteger services = BigInteger.ZERO;

        writeUint64(services);

        try {
            //InetAddress localhost = InetAddress.getByName("127.0.0.1");
            byte[] ipBytes = new  byte[16];
            writeBytes(ipBytes);

            writeInt((byte) (0xFF & 8333 >> 8));
            writeInt((byte) (0xFF & 8333));
        } catch (Exception e) {
            Log.i(App.TAG, "" + e.getMessage(), e);
        }

    }

    @Override
    protected void readPayload() {

    }

    @Override
    public String getCommandName() {
        return COMMAND_NAME;
    }
}
