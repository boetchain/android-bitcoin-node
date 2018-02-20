package com.boetchain.bitcoinnode.worker.thread;

import android.content.Context;

import com.boetchain.bitcoinnode.model.ChatLog;
import com.boetchain.bitcoinnode.model.Message.AddrMessage;
import com.boetchain.bitcoinnode.model.Message.AlertMessage;
import com.boetchain.bitcoinnode.model.Message.BaseMessage;
import com.boetchain.bitcoinnode.model.Message.GetAddrMessage;
import com.boetchain.bitcoinnode.model.Message.PingMessage;
import com.boetchain.bitcoinnode.model.Message.PongMessage;
import com.boetchain.bitcoinnode.model.Message.RejectMessage;
import com.boetchain.bitcoinnode.model.Message.SendCmpctMessage;
import com.boetchain.bitcoinnode.model.Message.SendHeadersMessage;
import com.boetchain.bitcoinnode.model.Message.VerAckMessage;
import com.boetchain.bitcoinnode.model.Message.VersionMessage;
import com.boetchain.bitcoinnode.model.Peer;
import com.boetchain.bitcoinnode.util.Lawg;
import com.boetchain.bitcoinnode.util.Util;
import com.boetchain.bitcoinnode.worker.broadcaster.PeerBroadcaster;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Collections;
import java.util.List;

/**
 * Created by Ross Badenhorst.
 */
public class PeerCommunicatorThread extends BaseThread {

    /**
     * Keeps track of consecutive message read failures. If no magic bytes are found or
     * the checksum fails more than MAX_FAILURE_COUNT for consecutive reads, this thread
     * will be interrupted and the peer removed.
     */
    public static final int MAX_FAILURE_COUNT = 10;

    private Socket socket;
    private PeerBroadcaster broadcaster;

    private boolean stayConnected = true;
    private int failureCount = 0;

    /**
     * The peer that this thread is making comms with.
     */
    private Peer peer;

    public PeerCommunicatorThread(Context context, Peer peer) {
        super(context);
        this.peer = peer;

        broadcaster = new PeerBroadcaster(context, peer);
    }

    @Override
    public void runThread() {
        onPeerConnectionAttempt();

        socket = new Socket();
        boolean success = connect(socket);

        try {
            if (success) {
                peer.timestamp = System.currentTimeMillis();
                peer.connected = true;
                peer.save();


                onPeerConencted();
                handlePeerMessages(socket.getOutputStream(), socket.getInputStream());
            } else {
                // Called when the connection has failed (often the connection isn't live yet)
                onPeerDisconnected();
            }
        } catch (IOException e) {
            // Called when the connected is live and is disconnected.
            onPeerDisconnected();
        }

        try {
            socket.getOutputStream().close();
            socket.getInputStream().close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Lets everyone know we are trying to connect to a peer.
     */
    private void onPeerConnectionAttempt() {
        broadcaster.broadcast(PeerBroadcaster.ACTION_PEER_CONNECTION_ATTEMPT);
    }

    /**
     * Lets every one know we have a connection with this peer.
     */
    private void onPeerConencted() {
        broadcaster.broadcast(PeerBroadcaster.ACTION_PEER_CONNECTED);
    }

    /**
     * Waits for a little bit before telling everyone the connection failed.
     * We often found that a little bump in the mobile connection and we would eat through
     * all the peers.
     */
    private void onPeerDisconnected() {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        peer.delete(); // Fuck this peer, lets try not talk to him

        broadcaster.broadcast(PeerBroadcaster.ACTION_PEER_DISCONNECTED);
    }

    /**
     * Connects to a peer.
     * If the socket times out or the handshake isn't completed.
     * We consider the connection a failure.
     * @param socket - to connect to a peer with
     *
     * @return true if connection success, false if not.
     */
    private boolean connect(Socket socket) {
        broadcaster.broadcastLog("connect: " + peer.address, ChatLog.TYPE_NEUTRAL);
        Lawg.i("connect: " + peer.address);

        InetSocketAddress address = new InetSocketAddress(peer.address, peer.port);

        try {
            socket.connect(address, 10000);

            OutputStream out = socket.getOutputStream();
            InputStream in = socket.getInputStream();

            VersionMessage versionMessage = new VersionMessage(); // Step 1 - send version
            writeMessage(versionMessage, out);

            VersionMessage peerVersionMessage = (VersionMessage) readMessage(in); // Step 2 - read peer version

            VerAckMessage verAckMessage = new VerAckMessage(); // Step 3 - write verAck
            writeMessage(verAckMessage, out);

            VerAckMessage peerVerAckMessage = (VerAckMessage) readMessage(in); // Step 4 - read verAk

            boolean success;
            if (peerVerAckMessage != null) {

                broadcaster.broadcastLog("Connection established", ChatLog.TYPE_NEUTRAL);
                Lawg.i("Connection established");
                peer.timestamp = System.currentTimeMillis();
                peer.connected = true;
                peer.save();
                success =  true;
            } else {
                broadcaster.broadcastLog("Failed to establish connection", ChatLog.TYPE_NEUTRAL);
                Lawg.e("Failed to establish connection");
                success = false;
            }

            return success;
        } catch (IOException e) {
            broadcaster.broadcastLog("Failed to establish connection", ChatLog.TYPE_NEUTRAL);
            Lawg.e("Failed to establish connection");
            return false;
        }
    }

    /**
     * Listens for peers messages and responds appropriately.
     *
     * @param out - channel to send messages on.
     * @param in - channel to receive messages on.
     * @throws IOException
     */
    private void handlePeerMessages(OutputStream out, InputStream in) throws IOException {
        writeMessage(new GetAddrMessage(), out); // Send out a sneaky getAddress message.

        while (stayConnected) {
            BaseMessage message = readMessage(in);

            if (message instanceof PingMessage) {
                writeMessage(new PongMessage(), out);
            }

            if (message instanceof AddrMessage) {
                List<Peer> addresses = ((AddrMessage) message).addresses;

                if (addresses.size() > Peer.MAX_POOL_SIZE) {

                    Collections.sort(addresses);
                    addresses = addresses.subList(0, Peer.MAX_POOL_SIZE - 1);
                }

                Peer.addPeersToPool(addresses);
            }
        }
    }

    /**
     * Writes a message to be sent to a peer.
     *
     * @param message - to be sent.
     * @param out - the stream we want to send the message on.
     */
    private void writeMessage(BaseMessage message, OutputStream out) {
        broadcaster.broadcastLog(message.getHumanReadableCommand(context),
                message.getCommandName(), ChatLog.TYPE_OUT);
        Lawg.i("--> " + message.getCommandName());

        byte[] header   = message.getHeader();
        byte[] payload  = message.getPayload();

        try {
            // Lawg.i("header: " + Util.bytesToHexString(header));
            //Lawg.i( "payload: " + Util.bytesToHexString(payload));

            out.write(header);
            out.write(payload);
            out.flush();
        } catch (IOException e) {
            broadcaster.broadcastLog("Failed to write message", ChatLog.TYPE_OUT);
            Lawg.e("Failed to write message");
        }
    }

    /**
     * Reads the incoming stream of bytes and tries to make sense of it.
     *
     * @param in - in coming message stream
     * @throws IOException - when shit happens.
     */
    private BaseMessage readMessage(InputStream in) throws IOException {
        //Lawg.i("readMessage");
        if (hasMagicBytes(in)) {

            byte[] header = readHeader(in);
            int payloadSize = getPayloadSizeFromHeader(header);
            byte[] checkSum = getCheckSumFromHeader(header);

            byte[] payload = readPayload(in, payloadSize);

            if (checkCheckSum(payload, checkSum)) {
                failureCount = 0;
                return constructMessage(header, payload);
            } else {

                failureCount++;
                checkFailure();
                Lawg.i("CheckSum failed....");
                return null;
            }
        } else {

            failureCount++;
            checkFailure();
            Lawg.i("no magic bytes found....");
            return null;
        }
    }

    private void checkFailure() {

        if (failureCount >= MAX_FAILURE_COUNT) {
            Peer.delete(peer);
            stayConnected = false;
        }
    }

    /**
     * The incoming stream can have a bunch of shit in front of it.
     * We need to ignore that, and look for our super special magic bytes.
     * If we find them, we know we have hit pay dirt and can read the rest of the message.
     * If nothing is found, we can assume our peer is drunk and needs to go home.
     *
     * @param in - in coming message stream
     * @return true is magic bytes are found, false if not.
     * @throws IOException - when shit happens.
     */
    private boolean hasMagicBytes(InputStream in) throws IOException {
        //Lawg.i("hasMagicBytes");
        byte[] superSpecialMagicBytes = new byte[BaseMessage.HEADER_LENGTH_MAGIC_BYTES];
        Util.addToByteArray(BaseMessage.MAGIC_PACKETS_MAINNET, 0, BaseMessage.HEADER_LENGTH_MAGIC_BYTES, superSpecialMagicBytes);

        int numMagicBytesFound  = 0;
        while (true) {
            int incomingByte = in.read();
            if (incomingByte == -1) {
                return false; // End of message
            }

            if (Util.byteToHexString((byte)incomingByte).contains(Util.byteToHexString(superSpecialMagicBytes[numMagicBytesFound]))) {
                //Lawg.i("numMagicBytesFound: " + numMagicBytesFound);
                numMagicBytesFound++;

                if (numMagicBytesFound == superSpecialMagicBytes.length) {
                    //Lawg.i("We found all the magic bytes...");
                    return true;
                }
            }
        }
    }

    /**
     * Gets the header from in the incoming message stream.
     * @param in - in coming message
     * @return header as a byte array
     * @throws IOException - when shit happens.
     */
    private byte[] readHeader(InputStream in) throws IOException {
        byte[] header = new byte[BaseMessage.HEADER_LENGTH_COMMAND + BaseMessage.HEADER_LENGTH_PAYLOAD_SIZE + BaseMessage.HEADER_LENGTH_CHECKSUM];

        int cursor = 0;
        while (cursor < header.length) {
            int bytesRead = in.read(header, cursor, header.length - cursor);
            if (bytesRead == -1) {
                break; // End of message
            }
            cursor += bytesRead;
        }

        return header;
    }

    /**
     * Gets the command name from the header.
     *
     * @param header - of the message
     * @return the command in string format.
     */
    private String getCommandNameFromHeader(byte[] header) {
        byte[] commandNameByteArray = new byte[BaseMessage.HEADER_LENGTH_COMMAND];
        for (int i = 0; i < header.length && i < BaseMessage.HEADER_LENGTH_COMMAND; i++) {
            commandNameByteArray[i] = header[i];
        }

        try {
            return new String(commandNameByteArray, "US-ASCII");
        } catch (UnsupportedEncodingException e) {
            return "";
        }
    }

    /**
     * Gets the payload size from the header.
     *
     * @param header - of the message
     * @return the payload size in bytes
     */
    private int getPayloadSizeFromHeader(byte[] header) {
        return (int) Util.readUint32(header, BaseMessage.HEADER_LENGTH_COMMAND);
    }

    /**
     * Gets the check sum from the header.
     *
     * @param header - of the message
     * @return the check sum
     */
    private byte[] getCheckSumFromHeader(byte[] header) {
        byte[] checksum = new byte[BaseMessage.HEADER_LENGTH_CHECKSUM];
        int checkSumOffsetInHeader = BaseMessage.HEADER_LENGTH_COMMAND + BaseMessage.HEADER_LENGTH_PAYLOAD_SIZE;
        System.arraycopy(header, checkSumOffsetInHeader, checksum, 0, BaseMessage.HEADER_LENGTH_CHECKSUM);

        return checksum;
    }

    /**
     * Gets the payload from the in coming byte stream.
     * By this stage, we should have read the header of the byte stream and therefore
     * should know how big the payload is.
     *
     * @param in - in coming message
     * @param payloadSize - the size of the payload, according to the header (lets hope our peer is right :?)
     * @return payload as a byte array
     * @throws IOException - when shit happens.
     */
    private byte[] readPayload(InputStream in, int payloadSize) throws IOException {
        byte[] payload = new byte[payloadSize];

        int cursor = 0;
        while (cursor < payload.length) {
            int bytesRead = in.read(payload, cursor, payload.length - cursor);
            //Lawg.i("br: "+ bytesRead);
            if (bytesRead == -1) {
                break; // End of message
            }
            cursor += bytesRead;
        }

        return payload;
    }

    /**
     * Verifies the checksum against the payload hasg.
     *
     * @param payload - of the incoming message
     * @param checksum - hash in the header
     * @return true, if yes, false if not.
     */
    private boolean checkCheckSum(byte[] payload, byte[] checksum) {
        byte[] hash = Util.doubleDigest(payload);
        if (checksum[0] != hash[0] || checksum[1] != hash[1] ||
                checksum[2] != hash[2] || checksum[3] != hash[3]) {
            return false;
        }

        return true;
    }

    /**
     * Creates a message from the byte array header
     * @param header
     * @param payload
     * @return
     */
    private BaseMessage constructMessage(byte[] header, byte[] payload) {

        BaseMessage message = null;

        String commandName = getCommandNameFromHeader(header);
        String commandText;
        Lawg.i("<-- " + commandName);

        if (commandName.toLowerCase().contains(RejectMessage.COMMAND_NAME)) {

            message = new RejectMessage(header, payload);
        } else if (commandName.toLowerCase().contains(VersionMessage.COMMAND_NAME)) {

            message = new VersionMessage(header, payload);
        } else if (commandName.toLowerCase().contains(VerAckMessage.COMMAND_NAME)) {

            message = new VerAckMessage(header, payload);
        } else if (commandName.toLowerCase().contains(AlertMessage.COMMAND_NAME)) {

            message = new AlertMessage(header, payload);
        } else if (commandName.toLowerCase().contains(AddrMessage.COMMAND_NAME)) {

            message = new AddrMessage(header, payload);
        } else if (commandName.toLowerCase().contains(SendHeadersMessage.COMMAND_NAME)) {

            message = new SendHeadersMessage(header, payload);
        } else if (commandName.toLowerCase().contains(SendCmpctMessage.COMMAND_NAME)) {

            message = new SendCmpctMessage(header, payload);
        } else if (commandName.toLowerCase().contains(PingMessage.COMMAND_NAME)) {

            message = new PingMessage(header, payload);
        }

        if (message != null) {

            commandText = message.getHumanReadableCommand(context);
        } else {

            commandText = BaseMessage.getDefaultHumanReadableCommand(context);
        }

        broadcaster.broadcastLog(commandText, commandName, ChatLog.TYPE_IN);

        return message;
    }

    public boolean isSocketConnected() {

        return socket != null && socket.isConnected();
    }

    public Peer getPeer() {
        return peer;
    }
}
