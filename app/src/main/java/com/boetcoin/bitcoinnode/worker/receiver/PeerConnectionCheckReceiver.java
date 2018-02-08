package com.boetcoin.bitcoinnode.worker.receiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.boetcoin.bitcoinnode.R;
import com.boetcoin.bitcoinnode.model.LogItem;
import com.boetcoin.bitcoinnode.model.Message.AddrMessage;
import com.boetcoin.bitcoinnode.model.Message.AlertMessage;
import com.boetcoin.bitcoinnode.model.Message.BaseMessage;
import com.boetcoin.bitcoinnode.model.Message.GetAddrMessage;
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
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Ross Badenhorst.
 */
public class PeerConnectionCheckReceiver extends BroadcastReceiver {
    public static final String TAG = PeerConnectionCheckReceiver.class.getSimpleName();

    public static final int RECEIVER_ID = 7710;

    /**
     * Max number of connections we want to maintain with peers
     */
    public static final int MAX_CONNECTIONS = 8;
    /**
     * How often we want to ping out peers to see if they are still alive.
     */
    public static final int CHECK_INTERVAL_SECONDS = 900; // 15 mins
    /**
     * Array of connected peers.
     */
    private ArrayList<Peer> connectedPeers = new ArrayList<>();

    public static String KEY_ACTION = "KEY_ACTION";

    public static int ACTION_STREAM_ALL = 100;
    public static int ACTION_GET_SINGLE_ADDRESS = 200;

    private Context context;

    @Override
    public void onReceive(final Context context, final Intent intent) {
        Lawg.i("PeerConnectionCheckReceiver starting...");

        final int action = intent.getIntExtra(KEY_ACTION, ACTION_STREAM_ALL);
        new Thread(new Runnable() {
            @Override
            public void run() {
                start(context, intent, action);
            }
        }).start();
    }

    private void start(Context context, Intent intent, int action) {
        this.context = context;
        connectedPeers = Peer.getConnectedPeers();

        if (connectedPeers.size() < MAX_CONNECTIONS) {
            connectToPeers();
        } else {
            if (action == ACTION_STREAM_ALL) {
                pingPeers();
            } else {
                connectToPeer(connectedPeers.get(new Random().nextInt(connectedPeers.size())));
            }
        }
    }

    private void connectToPeers() {
        Lawg.u(context, "Connecting to " + connectedPeers.size() + " peer(s)", LogItem.TI);

        int numberOfConnectedPeers = getNumberOfConnectedPeers();
        int numberOfNewConnectionsNeeded = (MAX_CONNECTIONS - numberOfConnectedPeers);

        while (numberOfNewConnectionsNeeded > 0) {
            Peer peerToConnect = findPeerToConnectTo();
            if (connectToPeer(peerToConnect)) {
                numberOfNewConnectionsNeeded--;
            }
        }

        Lawg.u(context, "We are now connected to : " + connectedPeers.size() + " peer(s)", LogItem.TI);
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
        //Lawg.i("findPeerToConnectTo");
        List<Peer> peerPool = Peer.listAll(Peer.class);
        if (peerPool.size() == 0) {
            peerPool = startDnsSeedDiscovery();
        }

        for (int i = 0; i < peerPool.size(); i++) {
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
    private List<Peer> startDnsSeedDiscovery() {
        Lawg.i("startDnsSeedDiscovery");
        String[] dnsSeeds = context.getResources().getStringArray(R.array.dns_seed_nodes);
        List<Peer> peerList = new ArrayList<>();

        for (String dnsSeed : dnsSeeds) {
            try {
                addPeersFromSeed(dnsSeed, peerList);
            } catch (UnknownHostException e) {
                Lawg.i("Failed to get peers from seed: " + dnsSeed);
            }
        }

        Peer.saveInTx(peerList);
        return peerList;
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
        Lawg.u(context, "Establishing a connection to: " + peer.ip + ":8333", LogItem.TI);

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

            boolean success;
            if (peerVerAckMessage != null) {
                getAddressesFromPeer(in, out);

                Lawg.u(context, " - Connection established", LogItem.TI);
                peer.timestamp = System.currentTimeMillis();
                peer.connected = true;
                peer.save();
                connectedPeers.add(peer);
                success =  true;
            } else {
                Lawg.u(context, " - Failed to establish connection", LogItem.TE);
                peer.delete(); // Fuck this peer, lets try not talk to him
                success = false;
            }

            out.close();
            in.close();
            socket.close();

            return success;
        } catch (IOException e) {
            Lawg.u(context, " - Failed to establish connection", LogItem.TE);
            peer.delete(); // Fuck this peer, lets try not talk to him
            return false;
        }
    }

    private void getAddressesFromPeer(InputStream in, OutputStream out) {

        GetAddrMessage getAddrMessage = new GetAddrMessage();
        writeMessage(getAddrMessage, out);

        int count = 0;
        while (count < 20) {

            try {

                BaseMessage msg = readMessage(in);

                if (msg instanceof PingMessage) {

                    PingMessage pingMessage = (PingMessage) msg;

                    //It's always nice to be acknowledged
                    PongMessage pongMessage = new PongMessage();
                    writeMessage(pongMessage, out);
                    count++;

                } else if (msg instanceof AddrMessage) {

                    AddrMessage addrMessage = (AddrMessage) msg;

                    //We're assuming if the array is greater than 1, he has
                    //not just sent us his own address and we struck gold
                    if (addrMessage.addresses.size() > 1) {
                        Lawg.u(context, " - " + addrMessage.addresses.size() + " addresses returned from peer", LogItem.TI);
                        // TODO save these peers to the DB (make sure to check your not saving any duplicates)
                        break;
                    }

                } else {

                    //we don't care about these, we want addresses
                    count++;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void pingPeers() {
        Lawg.u(context, "Trying to maintain connection with "+ connectedPeers.size() + " peers", LogItem.TV);

        for (Peer peer: connectedPeers) {
            pingPeer(peer);
        }
    }

    private boolean pingPeer(Peer peer) {
        Lawg.u(context, "Pinging peer:  " + peer.ip + ":8333", LogItem.TV);

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

            if (peerVerAckMessage != null) {
                Lawg.u(context, " - Ping successful", LogItem.TI);
                peer.timestamp = System.currentTimeMillis();
                peer.connected = true;
                peer.save();
            } else {
                Lawg.u(context, " - Ping failed", LogItem.TW);
                peer.delete();
            }

            out.close();
            in.close();
            socket.close();

            return true;
        } catch (IOException e) {
            Lawg.u(context, " - Failed to establish connection", LogItem.TE);
            peer.delete(); // Fuck this peer, lets try not talk to him
            return false;
        }
    }

    private void writeMessage(BaseMessage message, OutputStream out) {
        Lawg.u(context, "--->: " + message.getCommandName(), LogItem.TD);

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
        Lawg.u(context, "<---: " + commandName, LogItem.TD);

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

        return null;
    }

    public static void startServiceNow(Context context) {

        startServiceLater(context, 500);
    }

    public static void startServiceLater(Context context, long time) {

        Intent intent = new Intent(context, PeerConnectionCheckReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(context,
                RECEIVER_ID,
                intent,
                PendingIntent.FLAG_CANCEL_CURRENT);

        long millis = System.currentTimeMillis() + time;

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.set(AlarmManager.RTC_WAKEUP, millis, pi);
    }

    public static void cancelAlarm(Context context) {

        Intent intent = new Intent(context, PeerConnectionCheckReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, RECEIVER_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        alarmManager.cancel(pendingIntent);
    }
}
