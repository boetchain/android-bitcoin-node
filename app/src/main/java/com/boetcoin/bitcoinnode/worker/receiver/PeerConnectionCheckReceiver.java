package com.boetcoin.bitcoinnode.worker.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.boetcoin.bitcoinnode.R;
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
import com.boetcoin.bitcoinnode.util.Util;
import com.boetcoin.bitcoinnode.worker.service.BitcoinService;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ross Badenhorst.
 */
public class PeerConnectionCheckReceiver extends BroadcastReceiver {
    public static final String TAG = PeerConnectionCheckReceiver.class.getSimpleName();

    /**
     * Max number of connections we want to maintain with peers
     */
    public static final int MAX_CONNECTIONS = 8;
    /**
     * How often we want to ping out peers to see if they are still alive.
     */
    public static final int CHECK_INTERVAL_SECONDS = 60;
    /**
     * Array of connected peers.
     */
    private ArrayList<Peer> connectedPeers = new ArrayList<>();

    private Context context;

    @Override
    public void onReceive(final Context context, final Intent intent) {
        Log.i(TAG, "PeerConnectionCheckReceiver starting...");
        new Thread(new Runnable() {
            @Override
            public void run() {
                start(context, intent);
            }
        }).start();
    }

    private void start(Context context, Intent intent) {
        this.context = context;
        connectedPeers = Peer.getConnectedPeers();

        if (connectedPeers.size() < MAX_CONNECTIONS) {
            connectToPeers();
        } else {
            pingPeers();
        }
    }

    private void connectToPeers() {
        Log.i(TAG, "connectToPeers: " + connectedPeers.size());
        int numberOfConnectedPeers = getNumberOfConnectedPeers();
        int numberOfNewConnectionsNeeded = (BitcoinService.MAX_CONNECTIONS - numberOfConnectedPeers);

        while (numberOfNewConnectionsNeeded > 0) {
            Peer peerToConnect = findPeerToConnectTo();
            if (connectToPeer(peerToConnect)) {
                numberOfNewConnectionsNeeded--;
            }
        }
    }

    /**
     * @return number of peers we have an active connection with.
     */
    private int getNumberOfConnectedPeers() {
        int numberOfConnectedPeers = 0;
        for (Peer peer : connectedPeers) {
            if (peer != null && peer.connected) {
                numberOfConnectedPeers++;
            }
        }

        return numberOfConnectedPeers;
    }

    /**
     * Finds a peer to connect to.
     * Gets all the peers that we don't have a connection with,
     * then sorts them by the most recent peers first.
     * (We assume a peer we recently spoke to is probably a guy to speak to again).
     *
     * @return - a peer we can connect to.
     */
    private Peer findPeerToConnectTo() {
        //Log.i(TAG, "findPeerToConnectTo");
        List<Peer> peerPool = Peer.listAll(Peer.class);

        if (peerPool.size() == 0) {
            startDnsSeedDiscovery();
        }

        for (int i = 0; (i) < peerPool.size(); i++) {
            Peer peer  = peerPool.get(i);
            if (!peer.connected) {
                return peer;
            }
        }

        return null;
    }

    /**
     * Starts the DNS Seed peer discovery process.
     * We get a list of seeds that are hard coded in to the application.
     * From there we do a lookup to get a list of peers from the seed.
     */
    private void startDnsSeedDiscovery() {
        Log.i(TAG, "startDnsSeedDiscovery");
        String[] dnsSeeds = context.getResources().getStringArray(R.array.dns_seed_nodes);
        List<Peer> peerList = new ArrayList<>();

        for (String dnsSeed : dnsSeeds) {
            try {
                addPeersFromSeed(dnsSeed, peerList);
            } catch (UnknownHostException e) {
                Log.i(TAG, "Failed to get peers from seed: " + dnsSeed);
            }
        }

        Peer.saveInTx(peerList);
    }

    /**
     * Looks up peers from a DNS seed.
     * @param dnsSeed - that we ask for a list of peers.
     * @throws UnknownHostException - When shit happens.
     */
    private void addPeersFromSeed(String dnsSeed, List<Peer> peerList ) throws UnknownHostException {
        InetAddress[] peersFromDnsSeed = InetAddress.getAllByName(dnsSeed);

        for (InetAddress peerFromDnsSeed : peersFromDnsSeed) {
            peerList.add(new Peer(peerFromDnsSeed.getHostAddress()));
        }
    }

    private boolean connectToPeer(Peer peer) {
        Log.i(TAG, "connect to: " + peer.ip + ":8333");

        InetSocketAddress address = new InetSocketAddress(peer.ip, 8333);
        Socket socket = new Socket();
        try {
            socket.connect(address, 10000);

            OutputStream out = socket.getOutputStream();
            InputStream in = socket.getInputStream();

            // Step 1 - send version
            VersionMessage versionMessage = new VersionMessage();
            writeMessage(versionMessage, out);

            // Step 2 - read peer version
            VersionMessage peerVersionMessage = (VersionMessage) readMessage(in);

            // Step 3 - write verAck
            VerAckMessage verAckMessage = new VerAckMessage();
            writeMessage(verAckMessage, out);

            // Step 4 - read verAk
            VerAckMessage peerVerAckMessage = (VerAckMessage) readMessage(in);

            Log.i(TAG, "YAY! Connected to: " + peer.ip + ":8333");
            peer.timestamp = System.currentTimeMillis();
            peer.connected = true;
            peer.save();
            connectedPeers.add(peer);

            out.close();
            in.close();
            socket.close();

            return true;
        } catch (IOException e) {
            Log.e(TAG, "Failed to connect to: " + peer.ip);
            peer.delete(); // Fuck this peer, lets try not talk to him
            return false;
        }
    }

    private void pingPeers() {
        Log.i(TAG, "pingPeers: " + connectedPeers.size());

        for (Peer peer: connectedPeers) {
            pingPeer(peer);
        }
    }

    private boolean pingPeer(Peer peer) {
        Log.i(TAG, "pingPeer: " + peer.ip + ":8333");

        InetSocketAddress address = new InetSocketAddress(peer.ip, 8333);
        Socket socket = new Socket();
        try {
            socket.connect(address, 10000);

            OutputStream out = socket.getOutputStream();
            InputStream in = socket.getInputStream();

            // Step 1 - send version
            PingMessage pingMessage = new PingMessage();
            writeMessage(pingMessage, out);

            // Step 2 - read peer version
            PongMessage peerVersionMessage = (PongMessage) readMessage(in);

            Log.i(TAG, "YAY! Ping succesfull: " + peer.ip + ":8333");
            peer.timestamp = System.currentTimeMillis();
            peer.connected = true;
            peer.save();

            out.close();
            in.close();
            socket.close();

            return true;
        } catch (IOException e) {
            Log.e(TAG, "Failed to connect to: " + peer.ip);
            peer.delete(); // Fuck this peer, lets try not talk to him
            return false;
        }
    }

    private void writeMessage(BaseMessage message, OutputStream out) {
        Log.d(TAG, "writeMessage: " + message.getCommandName());

        byte[] header   = message.getHeader();
        byte[] payload  = message.getPayload();

        try {
            //Log.i(TAG,  "header: " + Util.bytesToHexString(header));
            //Log.i(TAG,  "payload: " + Util.bytesToHexString(payload));

            out.write(header);
            out.write(payload);
            out.flush();
        } catch (IOException e) {
            Log.i(TAG, "Failed to write message");
        }
    }

    /**
     * Reads the incoming stream of bytes and tries to make sense of it.
     *
     * @param in - in coming message stream
     * @throws IOException - when shit happens.
     */
    private BaseMessage readMessage(InputStream in) throws IOException {
        //Log.i(TAG, "readMessage");
        if (hasMagicBytes(in)) {

            byte[] header = readHeader(in);
            int payloadSize = getPayloadSizeFromHeader(header);
            byte[] checkSum = getCheckSumFromHeader(header);

            byte[] payload = readPayload(in, payloadSize);

            if (checkCheckSum(payload, checkSum)) {
                return constructMessage(header, payload);
            } else {
                Log.i(TAG, "CheckSum failed....");
                return null;
            }
        } else {
            Log.i(TAG, "no magic bytes found....");
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
        //Log.i(TAG, "hasMagicBytes");
        byte[] superSpecialMagicBytes = new byte[BaseMessage.HEADER_LENGTH_MAGIC_BYTES];
        Util.addToByteArray(BaseMessage.MAGIC_PACKETS_MAINNET, 0, BaseMessage.HEADER_LENGTH_MAGIC_BYTES, superSpecialMagicBytes);

        int numMagicBytesFound  = 0;
        while (true) {
            int incomingByte = in.read();
            if (incomingByte == -1) {
                return false; // End of message
            }

            if (Util.byteToHexString((byte)incomingByte).contains(Util.byteToHexString(superSpecialMagicBytes[numMagicBytesFound]))) {
                //Log.i(TAG, "numMagicBytesFound: " + numMagicBytesFound);
                numMagicBytesFound++;

                if (numMagicBytesFound == superSpecialMagicBytes.length) {
                    //Log.i(TAG, "We found all the magic bytes...");
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
            //Log.i(TAG, "br: "+ bytesRead);
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
        Log.i(TAG, "Constructing: " + commandName);

        if (commandName.toLowerCase().contains(RejectMessage.COMMAND_NAME)) {
            RejectMessage rejectMessage = new RejectMessage(header, payload);
            //Log.i(TAG, rejectMessage.toString());
            return  rejectMessage;
        }

        if (commandName.toLowerCase().contains(VersionMessage.COMMAND_NAME)) {
            VersionMessage versionMessage = new VersionMessage(header, payload);
            //Log.i(TAG, versionMessage.toString());
            return versionMessage;
        }

        if (commandName.toLowerCase().contains(VerAckMessage.COMMAND_NAME)) {
            VerAckMessage verAckMessage = new VerAckMessage(header, payload);
            //Log.i(TAG, verAckMessage.toString());
            return new VerAckMessage(header, payload);
        }

        if (commandName.toLowerCase().contains(AlertMessage.COMMAND_NAME)) {
            AlertMessage alertMessage = new AlertMessage(header, payload);
            //Log.i(TAG, alertMessage.toString());
            return new VerAckMessage(header, payload);
        }

        if (commandName.toLowerCase().contains(AddrMessage.COMMAND_NAME)) {
            AddrMessage addrMessage = new AddrMessage(header, payload);
            //Log.i(TAG, addrMessage.toString());
            return addrMessage;
        }

        if (commandName.toLowerCase().contains(SendHeadersMessage.COMMAND_NAME)) {
            SendHeadersMessage sendHeadersMessage = new SendHeadersMessage(header, payload);
            //Log.i(TAG, sendHeadersMessage.toString());
            return sendHeadersMessage;
        }

        if (commandName.toLowerCase().contains(SendCmpctMessage.COMMAND_NAME)) {
            SendCmpctMessage sendCmpctMessage = new SendCmpctMessage(header, payload);
            //Log.i(TAG, sendCmpctMessage.toString());
            return sendCmpctMessage;
        }

        return null;
    }
}
