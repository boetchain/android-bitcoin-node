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

    ArrayList<Integer> response = new ArrayList<>();

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
                    connect(locallySavedPeers.get(1));
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
            Log.i(App.TAG,  "header: " + Util.bytesToHexString(header));
            Log.i(App.TAG,  "payload: " + Util.bytesToHexString(payload));

            out.write(header);
            out.write(payload);
        } catch (IOException e) {
            Log.i(App.TAG, "Failed to write message");
        }

    }

    private void readMessage(InputStream in) throws IOException {
        Log.i(App.TAG, "readMessage");

        while (true) {
            int b = in.read();
            Log.i(App.TAG, "read: " + b);

            if (b == -1) {
                Log.i(App.TAG, "END OF CONNECTION!");
                break;
            }

            response.add(b);

        }
    }
}
