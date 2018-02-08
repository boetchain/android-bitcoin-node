package com.boetcoin.bitcoinnode.ui.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.boetcoin.bitcoinnode.App;
import com.boetcoin.bitcoinnode.R;
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
import com.boetcoin.bitcoinnode.util.Notify;
import com.boetcoin.bitcoinnode.util.Prefs;
import com.boetcoin.bitcoinnode.util.Util;
import com.boetcoin.bitcoinnode.worker.receiver.PeerConnectionCheckReceiver;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    static int count = 0;

    private boolean isTuningHowzit = false;

    private Button howzitBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        howzitBtn = (Button) findViewById(R.id.activity_main_howzit_btn);
        howzitBtn.setOnClickListener(this);
    }

    private void tuneHowzit() {

        toggleHowzitBtnState(true);
        PeerConnectionCheckReceiver.startServiceNow(this);
    }

    private void getAddresses(Peer peer) {
        Log.i(App.TAG, "connect to: " + peer.ip + ":8333");

        InetSocketAddress address = new InetSocketAddress(peer.ip, 8333);
        Socket socket = new Socket();
        try {
            socket.connect(address, 10000);

            OutputStream out = socket.getOutputStream();
            InputStream in = socket.getInputStream();

            // Step 1 - send version
            VersionMessage versionMessage = new VersionMessage();
            writeMessage(versionMessage, out);
            Log.i(App.TAG, "our version: " + versionMessage.toString());

            // Step 2 - read peer version
            VersionMessage peerVersionMessage = (VersionMessage) readMessage(in);

            // Step 4 - read verAk
            VerAckMessage peerVerAckMessage = (VerAckMessage) readMessage(in);

            // Step 3 - write verAck
            VerAckMessage verAckMessage = new VerAckMessage();
            writeMessage(verAckMessage, out);

            // Step 4 - wite getAddresses
            GetAddrMessage getAddrMessage = new GetAddrMessage();
            writeMessage(getAddrMessage, out);

            // Step 5 - wait for the response we want
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
    private BaseMessage readMessage(InputStream in) throws IOException {
        Log.i(App.TAG, "readMessage");
        if (hasMagicBytes(in)) {

            byte[] header = readHeader(in);
            int payloadSize = getPayloadSizeFromHeader(header);
            byte[] checkSum = getCheckSumFromHeader(header);

            byte[] payload = readPayload(in, payloadSize);

            if (checkCheckSum(payload, checkSum)) {
                return constructMessage(header, payload);
            } else {
                Log.i(App.TAG, "CheckSum failed....");
                return null;
            }
        } else {
            Log.i(App.TAG, "no magic bytes found....");
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
        //Log.i(App.TAG, "hasMagicBytes");
        byte[] superSpecialMagicBytes = new byte[BaseMessage.HEADER_LENGTH_MAGIC_BYTES];
        Util.addToByteArray(BaseMessage.MAGIC_PACKETS_MAINNET, 0, BaseMessage.HEADER_LENGTH_MAGIC_BYTES, superSpecialMagicBytes);

        int numMagicBytesFound  = 0;
        while (true) {
            int incomingByte = in.read();
            if (incomingByte == -1) {
                return false; // End of message
            }

            if (Util.byteToHexString((byte)incomingByte).contains(Util.byteToHexString(superSpecialMagicBytes[numMagicBytesFound]))) {
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

    private BaseMessage constructMessage(byte[] header, byte[] payload) {
        String commandName = getCommandNameFromHeader(header);
        Log.i(App.TAG, "Constructing: " + commandName);

        if (commandName.toLowerCase().contains(RejectMessage.COMMAND_NAME)) {
            RejectMessage rejectMessage = new RejectMessage(header, payload);
            Log.i(App.TAG, rejectMessage.toString());
            return  rejectMessage;
        }

        if (commandName.toLowerCase().contains(VersionMessage.COMMAND_NAME)) {
            VersionMessage versionMessage = new VersionMessage(header, payload);
            Log.i(App.TAG, versionMessage.toString());
            return versionMessage;
        }

        if (commandName.toLowerCase().contains(VerAckMessage.COMMAND_NAME)) {
            VerAckMessage verAckMessage = new VerAckMessage(header, payload);
            Log.i(App.TAG, verAckMessage.toString());
            return new VerAckMessage(header, payload);
        }

        if (commandName.toLowerCase().contains(AlertMessage.COMMAND_NAME)) {
            AlertMessage alertMessage = new AlertMessage(header, payload);
            Log.i(App.TAG, alertMessage.toString());
            return new VerAckMessage(header, payload);
        }

        if (commandName.toLowerCase().contains(AddrMessage.COMMAND_NAME)) {
            AddrMessage addrMessage = new AddrMessage(header, payload);
            Log.i(App.TAG, addrMessage.toString());
            return addrMessage;
        }

        if (commandName.toLowerCase().contains(SendHeadersMessage.COMMAND_NAME)) {
            SendHeadersMessage sendHeadersMessage = new SendHeadersMessage(header, payload);
            Log.i(App.TAG, sendHeadersMessage.toString());
            return sendHeadersMessage;
        }

        if (commandName.toLowerCase().contains(SendCmpctMessage.COMMAND_NAME)) {
            SendCmpctMessage sendCmpctMessage = new SendCmpctMessage(header, payload);
            Log.i(App.TAG, sendCmpctMessage.toString());
            return sendCmpctMessage;
        }

        if (commandName.toLowerCase().contains(PongMessage.COMMAND_NAME)) {
            PongMessage pongMessage = new PongMessage(header, payload);
            Log.i(App.TAG, pongMessage.toString());
            return pongMessage;
        }

        if (commandName.toLowerCase().contains(PingMessage.COMMAND_NAME)) {
            PingMessage pingMessage = new PingMessage(header, payload);
            Log.i(App.TAG, pingMessage.toString());
            return pingMessage;
        }

        return null;
    }

    private void saveResponseLocally(InputStream in) throws IOException {
        Log.i(App.TAG, "saveResponseLocally");
        ArrayList<Integer> respList = new ArrayList<>();

        while (true) {
            int b = in.read();

            respList.add(b);
            Log.i(App.TAG, "" + respList.size());
            if (b == -1 || respList.size() > 10000) {
                byte[] bb = new byte[respList.size()];

                for (int i = 0; i < bb.length; i++) {
                    bb[i] = respList.get(i).byteValue();
                }

                Prefs.put(this, "res", bb);

                Log.i(App.TAG, "save complete?");
                return;
            }
        }
    }

    /**
     * Sets the howzit button to working/not working
     *
     * @param startTuning determines whether to set the state as working or not working
     */
    private void toggleHowzitBtnState(boolean startTuning) {

        if (startTuning) {

            howzitBtn.setText(getResources().getString(R.string.activity_main_howzit_btn_start_working));
        } else {

            howzitBtn.setText(getResources().getString(R.string.activity_main_howzit_btn));
        }
        isTuningHowzit = startTuning;
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.activity_main_howzit_btn:
                if (!isTuningHowzit) {
                    howzitBtn.setText(getString(R.string.activity_main_howzit_btn_start_working));
                    tuneHowzit();
                } else {
                    Notify.toast(this, R.string.activity_main_howzit_btn_toast_busy, Toast.LENGTH_SHORT);
                }
            break;
        }
    }
}
