package com.boetcoin.bitcoinnode.worker.thread;

import android.content.Context;
import android.support.v4.content.LocalBroadcastManager;

import com.boetcoin.bitcoinnode.model.Message.AddrMessage;
import com.boetcoin.bitcoinnode.model.Message.AlertMessage;
import com.boetcoin.bitcoinnode.model.Message.BaseMessage;
import com.boetcoin.bitcoinnode.model.Message.PingMessage;
import com.boetcoin.bitcoinnode.model.Message.PongMessage;
import com.boetcoin.bitcoinnode.model.Message.RejectMessage;
import com.boetcoin.bitcoinnode.model.Message.SendCmpctMessage;
import com.boetcoin.bitcoinnode.model.Message.SendHeadersMessage;
import com.boetcoin.bitcoinnode.model.Message.VerAckMessage;
import com.boetcoin.bitcoinnode.model.Message.VersionMessage;
import com.boetcoin.bitcoinnode.model.Peer;
import com.boetcoin.bitcoinnode.util.Lawg;
import com.boetcoin.bitcoinnode.util.Util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Created by Ross Badenhorst.
 */
public class PeerCommunicatorThread extends BaseThread {

    private Peer peer;

    public PeerCommunicatorThread(Context context, Peer peer) {
        super(context);
        this.peer = peer;
    }

    @Override
    public void run() {
        Socket socket = new Socket();
        boolean success = connect(socket);

        try {
            if (success) {
                peer.timestamp = System.currentTimeMillis();
                peer.connected = true;
                peer.save();

                handlePeerMessages(socket.getOutputStream(), socket.getInputStream());
            } else {
                peer.delete(); // Fuck this peer, lets try not talk to him
            }
        } catch (IOException e) {
        }

        try {
            socket.getOutputStream().close();
            socket.getInputStream().close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean connect(Socket socket) {
        Lawg.i("connect: " + peer.ip);

        InetSocketAddress address = new InetSocketAddress(peer.ip, 8333);

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

                Lawg.i(" - Connection established");
                peer.timestamp = System.currentTimeMillis();
                peer.connected = true;
                peer.save();
                success =  true;
            } else {
                Lawg.i(" - Failed to establish connection");
                success = false;
            }

            return success;
        } catch (IOException e) {
            return false;
        }
    }

    private void handlePeerMessages(OutputStream out, InputStream in) throws IOException {
        while (true) {
            BaseMessage message = readMessage(in);

            if (message instanceof PingMessage) {
                writeMessage(new PongMessage(), out);
            }
        }
    }

    // Util Method
    private void writeMessage(BaseMessage message, OutputStream out) {
        Lawg.i("---> " + message.getCommandName());

        byte[] header   = message.getHeader();
        byte[] payload  = message.getPayload();

        try {
            // Lawg.i("header: " + Util.bytesToHexString(header));
            //Lawg.i( "payload: " + Util.bytesToHexString(payload));

            out.write(header);
            out.write(payload);
            out.flush();
        } catch (IOException e) {
            Lawg.i("Failed to write message");
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
                return constructMessage(header, payload);
            } else {
                Lawg.i("CheckSum failed....");
                return null;
            }
        } else {
            Lawg.i("no magic bytes found....");
            return null;
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

    private BaseMessage constructMessage(byte[] header, byte[] payload) {
        String commandName = getCommandNameFromHeader(header);
        Lawg.i("<--- " + commandName);

        if (commandName.toLowerCase().contains(RejectMessage.COMMAND_NAME)) {
            RejectMessage rejectMessage = new RejectMessage(header, payload);
            //Lawg.i(rejectMessage.toString());
            return  rejectMessage;
        }

        if (commandName.toLowerCase().contains(VersionMessage.COMMAND_NAME)) {
            VersionMessage versionMessage = new VersionMessage(header, payload);
            //Lawg.i(versionMessage.toString());
            return versionMessage;
        }

        if (commandName.toLowerCase().contains(VerAckMessage.COMMAND_NAME)) {
            VerAckMessage verAckMessage = new VerAckMessage(header, payload);
            //Lawg.i(verAckMessage.toString());
            return new VerAckMessage(header, payload);
        }

        if (commandName.toLowerCase().contains(AlertMessage.COMMAND_NAME)) {
            AlertMessage alertMessage = new AlertMessage(header, payload);
            //Lawg.i(alertMessage.toString());
            return new VerAckMessage(header, payload);
        }

        if (commandName.toLowerCase().contains(AddrMessage.COMMAND_NAME)) {
            AddrMessage addrMessage = new AddrMessage(header, payload);
            //Lawg.i(addrMessage.toString());
            return addrMessage;
        }

        if (commandName.toLowerCase().contains(SendHeadersMessage.COMMAND_NAME)) {
            SendHeadersMessage sendHeadersMessage = new SendHeadersMessage(header, payload);
            //Lawg.i(sendHeadersMessage.toString());
            return sendHeadersMessage;
        }

        if (commandName.toLowerCase().contains(SendCmpctMessage.COMMAND_NAME)) {
            SendCmpctMessage sendCmpctMessage = new SendCmpctMessage(header, payload);
            //Lawg.i(sendCmpctMessage.toString());
            return sendCmpctMessage;
        }

        if (commandName.toLowerCase().contains(PingMessage.COMMAND_NAME)) {
            PingMessage pingMessage = new PingMessage(header, payload);
            //Lawg.i(sendCmpctMessage.toString());
            return pingMessage;
        }

        return null;
    }
}
