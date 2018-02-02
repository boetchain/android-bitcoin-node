package com.boetcoin.bitcoinnode.ui.activity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.boetcoin.bitcoinnode.App;
import com.boetcoin.bitcoinnode.R;
import com.boetcoin.bitcoinnode.model.Message.BaseMessage;
import com.boetcoin.bitcoinnode.model.Message.VersionMessage;
import com.boetcoin.bitcoinnode.model.Peer;
import com.boetcoin.bitcoinnode.util.Util;
import com.google.common.primitives.Longs;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final List<Peer> locallySavedPeers = Peer.listAll(Peer.class);
        Log.i(App.TAG, "locallySavedPeers: " + locallySavedPeers.size());

        if (locallySavedPeers.size() == 0) {
            Peer.findByDnsSeeds(getResources().getStringArray(R.array.dns_seed_nodes));
        } else {

            new AsyncTask<Void, Void, Void>() {
                protected Void doInBackground(Void... unused) {
                    //VersionMessage versionMessage = new VersionMessage();
                    //Log.i(App.TAG, versionMessage.toString());
                    connect(locallySavedPeers.get(2));
                    return null;
                }
            }.execute();


        }
    }

    private void connect(Peer peer) {
        Log.i(App.TAG, "connect to: " + peer.ip + ":8333");

        InetSocketAddress address = new InetSocketAddress(peer.ip, 8333);
        Socket socket = new Socket();
        try {
            socket.connect(address, 10000);

            OutputStream out = socket.getOutputStream();
            InputStream in = socket.getInputStream();

            VersionMessage versionMessage = new VersionMessage();
            writeMessage(versionMessage, out);

            readMessage(in);

            Log.i(App.TAG, "Shutting down....");
            out.close();
            in.close();
            socket.close();

        } catch (IOException e) {
            Log.i(App.TAG, "Socket failed to conenct");
        }
    }

    private void writeMessage(BaseMessage message, OutputStream out) {
        Log.i(App.TAG, "writeMessage: " + message.getCommandName());

        byte[] header   = message.getHeader();
        byte[] payload  = message.getPayload();

        try {
            //Log.i(App.TAG,  "header: " + Util.bytesToHexString(header));
            //Log.i(App.TAG,  "payload: " + Util.bytesToHexString(payload));

            out.write(header);
            out.write(payload);
        } catch (IOException e) {
            Log.i(App.TAG, "Failed to write message");
        }

    }

    /**
     * Constructs the incomming mesage into a byte array list.
     * We then can interrogate this arraylist to the and figure out
     * what our peer is saying to us.
     *
     * @param in - incoming input stream from out peer
     * @throws IOException
     */
    private void readMessage(InputStream in) throws IOException {
        Log.i(App.TAG, "readingMessage...");

        ArrayList<Byte> incomingMessage = new ArrayList<>();
        constructIncomingMessage(in, incomingMessage);

        int readCursor = getStartOfMagicBytes(incomingMessage);
        if (readCursor != -1) {

            byte[] header       = readHeader(incomingMessage, readCursor);
            String commandName  = getCommandNameFromHeader(header);
            int payloadSize     = getPayloadSizeFromHeader(header);
            Log.i(App.TAG, "commandName " + commandName);
            Log.i(App.TAG, "payloadSize " + payloadSize);
        } else {
            Log.i(App.TAG, "No magic bytes found");
        }
    }

    /**
     * Reads the input stream and saves the results to a byte arraylist.
     * @param in - in coming input stream.
     * @param incomingMessage - byte araylist to save the input stream results tp
     * @throws IOException
     */
    private void constructIncomingMessage(InputStream in, ArrayList<Byte> incomingMessage) throws IOException {
        while (true) {
            int b = in.read();
            if (b == -1) {
                Log.i(App.TAG, " - End of message....");
                break;
            }

            incomingMessage.add((byte) b);
        }
    }

    /**
     * Looks complicated but it is quite simple, it looks for a sequence of bytes (the magic bytes) in
     * the incoming message.
     *
     * If we find all the bytes in the correct sequence we return this position, so we can read the rest of the message.
     * If we cant find the bytes in the correct sequence we return -1.
     *
     * @param incomingMessage - the message from our peer
     * @return the position at which the magic bytes start.
     */
    private int getStartOfMagicBytes(ArrayList<Byte> incomingMessage) {
        byte[] magicPackets = new byte[BaseMessage.HEADER_MAGIC_STRING_LENGTH];
        Util.addToByteArray(BaseMessage.PACKET_MAGIC_MAINNET, 0, BaseMessage.HEADER_MAGIC_STRING_LENGTH, magicPackets);

        int magicBytesStart     = -1;
        int numMagicBytesFound  = 0;
        for (int i = 0; i < incomingMessage.size(); i++) {

            if (incomingMessage.get(i) == magicPackets[numMagicBytesFound]) {
                numMagicBytesFound++;

                if (numMagicBytesFound == magicPackets.length) {
                    magicBytesStart = ((magicPackets.length - 1) - i);
                    return magicBytesStart;
                }
            } else {
                numMagicBytesFound = 0;
                magicBytesStart = -1;
            }
        }

        return magicBytesStart;
    }

    /**
     * Creates the header from the incomming message.
     * At this point we know where the magic bytes, we can create the header and from the bytes after its postion.
     *
     * @param incomingMessage - the entire incoming message from the peer.
     * @param readCursor - the position at the last magic byte.
     * @return a nice neat header to figure out, what the crap is going on.
     */
    private byte[] readHeader(ArrayList<Byte> incomingMessage, int readCursor) {
        byte[] header = new byte[BaseMessage.HEADER_MAGIC_STRING_LENGTH + BaseMessage.HEADER_COMMAND_LENGTH + BaseMessage.HEADER_PAYLOAD_SIZE_LENGTH + BaseMessage.HEADER_CHECKSUM_LENGTH];

        for (int i = 0; i < header.length && readCursor < incomingMessage.size(); i++) {
            header[i] = incomingMessage.get(readCursor);
            readCursor++;
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

        byte[] commandNameByteArray = new byte[BaseMessage.HEADER_COMMAND_LENGTH];
        for (int i = 0; i < header.length && i < BaseMessage.HEADER_COMMAND_LENGTH; i++) {
                commandNameByteArray[i] = header[i + BaseMessage.HEADER_MAGIC_STRING_LENGTH]; // The command is after the packet magic
        }

        return new String(commandNameByteArray);
    }

    /**
     * Gets the payload size from the header.
     * @param header - of the message
     * @return the payload size in bytes
     */
    private int getPayloadSizeFromHeader(byte[] header) {

        byte[] payloadSizeByteArray = new byte[BaseMessage.HEADER_PAYLOAD_SIZE_LENGTH];
        for (int i = 0; i < header.length && i < BaseMessage.HEADER_PAYLOAD_SIZE_LENGTH; i++) {
            payloadSizeByteArray[i] = header[i + BaseMessage.HEADER_MAGIC_STRING_LENGTH + BaseMessage.HEADER_COMMAND_LENGTH]; // The payload size is after the packet magic & command
        }
        
        ByteBuffer wrapped = ByteBuffer.wrap(payloadSizeByteArray); // big-endian by default
        return  wrapped.getShort();
    }
}
