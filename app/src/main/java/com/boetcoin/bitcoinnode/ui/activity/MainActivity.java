package com.boetcoin.bitcoinnode.ui.activity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.boetcoin.bitcoinnode.App;
import com.boetcoin.bitcoinnode.R;
import com.boetcoin.bitcoinnode.model.Message.BaseMessage;
import com.boetcoin.bitcoinnode.model.Message.RejectMessage;
import com.boetcoin.bitcoinnode.model.Message.VersionMessage;
import com.boetcoin.bitcoinnode.model.Msg.BaseMsg;
import com.boetcoin.bitcoinnode.model.Msg.RejectMsg;
import com.boetcoin.bitcoinnode.model.Msg.VersionMsg;
import com.boetcoin.bitcoinnode.model.Peer;
import com.boetcoin.bitcoinnode.util.Prefs;
import com.boetcoin.bitcoinnode.util.Util;
import com.google.common.collect.ObjectArrays;
import com.google.common.primitives.Bytes;
import com.google.common.primitives.Longs;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //final byte[] savedResponse = Prefs.getByte(this, "res", new byte[0]);
        //VersionMessage versionMessage = new VersionMessage();

        VersionMsg versionMessage = new VersionMsg();
        final byte[] savedResponse = Bytes.concat(versionMessage.getHeader(), versionMessage.getPayload());

        /*
        final byte[] savedResponse = new byte[1000];
        for (int i = 0; i < savedResponse.length ; i++) {
            savedResponse[i] = 1;
        }
        */


        //Log.i(App.TAG, "UI: " + Util.bytesToHexString(Util.doubleDigest(versionMessage.getPayload())));


        //Log.i(App.TAG, "LEN P : " +  versionMessage.getPayload().length);
        //Log.i(App.TAG, "LEN S : " +  savedResponse.length);
        //Log.i(App.TAG, Util.bytesToHexString(savedResponse));

        //Log.i(App.TAG, Util.bytesToHexString(Bytes.concat(versionMessage.getHeader(), versionMessage.getPayload())));
        //Log.i(App.TAG, "LEN ALL : " +  savedResponse.length);

        InputStream in = new InputStream() {
            int pos = 0;
            @Override
            public int read() throws IOException {

                if (pos < savedResponse.length) {
                    //Log.i(App.TAG, "read : " +  pos + " | " + savedResponse.length ) ;
                    int b = savedResponse[pos];
                    pos++;
                    return b;
                }

                //Log.i(App.TAG, "pos : " +  pos);
                return -1;
            }
        };

        try {
            readMessage(in);
        } catch (Exception e) {
            Log.i(App.TAG, "Read message failed: " + e.getMessage());
        }



        /*
        //Peer.deleteAll(Peer.class);
        final List<Peer> locallySavedPeers = Peer.listAll(Peer.class);
        Log.i(App.TAG, "locallySavedPeers: " + locallySavedPeers.size());

        if (locallySavedPeers.size() == 0) {
            Peer.findByDnsSeeds(getResources().getStringArray(R.array.dns_seed_nodes));
        } else {

            new AsyncTask<Void, Void, Void>() {
                protected Void doInBackground(Void... unused) {
                    //VersionMessage versionMessage = new VersionMessage();
                    //Log.i(App.TAG, versionMessage.toString());
                    connect(locallySavedPeers.get(0));
                    return null;
                }
            }.execute();
        }
        */
    }

    private void connect(Peer peer) {
        Log.i(App.TAG, "connect to: " + peer.ip + ":8333");

        InetSocketAddress address = new InetSocketAddress(peer.ip, 8333);
        Socket socket = new Socket();
        try {
            socket.connect(address, 10000);

            OutputStream out = socket.getOutputStream();
            InputStream in = socket.getInputStream();

            VersionMsg versionMessage = new VersionMsg();
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

    private void writeMessage(BaseMsg message, OutputStream out) {
        Log.i(App.TAG, "writeMessage: " + message.getCommandName());

        byte[] header   = message.getHeader();
        byte[] payload  = message.getPayload();

        try {
            //Log.i(App.TAG,  "header: " + Util.bytesToHexString(header));
            //Log.i(App.TAG,  "payload: " + Util.bytesToHexString(payload));

            out.write(header);
            out.write(payload);
            out.flush();
        } catch (IOException e) {
            Log.i(App.TAG, "Failed to write message");
        }
    }

    /**
     * Reads the incoming stream of bytes and tries to make sense of it.
     *
     * @param in - in coming message stream
     * @throws IOException - when shit happens.
     */
    private void readMessage(InputStream in) throws IOException {
        if (hasMagicBytes(in)) {

            byte[] header = readHeader(in);
            int payloadSize = getPayloadSizeFromHeader(header);
            byte[] checkSum = getCheckSumFromHeader(header);

            byte[] payload = readPayload(in, payloadSize);

            //Log.i(App.TAG, "ps: " + payloadSize);
            //Log.i(App.TAG, "fs: " + payload.length);
            //Log.i(App.TAG, "rm: " + Util.bytesToHexString(Util.doubleDigest(payload)));
            //Log.i(App.TAG, Util.bytesToHexString(payload));

            //byte[] x = Bytes.concat(header, payload);
            //Log.i(App.TAG, Util.bytesToHexString(x));


            if (checkCheckSum(payload, checkSum)) {
                constructMessage(header, payload);
            } else {
                Log.i(App.TAG, "CheckSum failed....");
            }

        } else {
            Log.i(App.TAG, "no magic bytes found....");
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
        //Log.i(App.TAG, "searchForMagicBytes");
        byte[] superSpecialMagicBytes = new byte[BaseMessage.HEADER_MAGIC_STRING_LENGTH];
        Util.addToByteArray(BaseMessage.PACKET_MAGIC_MAINNET, 0, BaseMessage.HEADER_MAGIC_STRING_LENGTH, superSpecialMagicBytes);

        int numMagicBytesFound  = 0;
        while (true) {
            int incomingByte = in.read();
            if (incomingByte == -1) {
                return false; // End of message
            }

            if (incomingByte  == superSpecialMagicBytes[numMagicBytesFound]) {
                //Log.i(App.TAG, "numMagicBytesFound: " + numMagicBytesFound);
                numMagicBytesFound++;

                if (numMagicBytesFound == superSpecialMagicBytes.length) {
                    //Log.i(App.TAG, "We found all the magic bytes...");
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
        byte[] header = new byte[BaseMessage.HEADER_COMMAND_LENGTH + BaseMessage.HEADER_PAYLOAD_SIZE_LENGTH + BaseMessage.HEADER_CHECKSUM_LENGTH];

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
        byte[] commandNameByteArray = new byte[BaseMessage.HEADER_COMMAND_LENGTH];
        for (int i = 0; i < header.length && i < BaseMessage.HEADER_COMMAND_LENGTH; i++) {
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
        return (int) Util.readUint32(header, BaseMessage.HEADER_COMMAND_LENGTH);
    }

    /**
     * Gets the check sum from the header.
     *
     * @param header - of the message
     * @return the check sum
     */
    private byte[] getCheckSumFromHeader(byte[] header) {
        byte[] checksum = new byte[BaseMessage.HEADER_CHECKSUM_LENGTH];
        int checkSumOffsetInHeader = BaseMessage.HEADER_COMMAND_LENGTH + BaseMessage.HEADER_PAYLOAD_SIZE_LENGTH;
        System.arraycopy(header, checkSumOffsetInHeader, checksum, 0, BaseMessage.HEADER_CHECKSUM_LENGTH);

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
            //Log.i(App.TAG, "br: "+ bytesRead);
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

    private void constructMessage(byte[] header, byte[] payload) {
        String commandName = getCommandNameFromHeader(header);
        Log.i(App.TAG, "Constructing: " + commandName);

        BaseMsg message;
        if (commandName.toLowerCase().contains(RejectMessage.COMMAND_NAME)) {
            message = new RejectMsg(header, payload);
            Log.i(App.TAG, message.toString());
        }

        if (commandName.toLowerCase().contains(VersionMsg.COMMAND_NAME)) {
            message = new VersionMsg(header, payload);
            Log.i(App.TAG, message.toString());
        }
    }
}
