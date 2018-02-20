package com.boetchain.bitcoinnode.ui.activity;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.boetchain.bitcoinnode.App;
import com.boetchain.bitcoinnode.R;
import com.boetchain.bitcoinnode.model.Peer;
import com.boetchain.bitcoinnode.network.request.GETGeolocationFromIpRequest;
import com.boetchain.bitcoinnode.network.response.GETGeolocationFromIpResponse;
import com.boetchain.bitcoinnode.ui.adapter.PeerAdapter;
import com.boetchain.bitcoinnode.ui.adapter.StatusAdapter;
import com.boetchain.bitcoinnode.ui.view.DrawerHeaderView;
import com.boetchain.bitcoinnode.util.Lawg;
import com.boetchain.bitcoinnode.worker.broadcaster.PeerBroadcaster;
import com.boetchain.bitcoinnode.worker.service.PeerManagementService;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ross Badenhorst.
 */
public class MainActivity extends BaseActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

    /**
     * The number of actual menu items in the drawer menu EXCLUDING header and footer items
     */
    public static int DRAWER_MENU_SIZE = 1;

    /**
     * These are the indexes of menu items in the drawer nav
     */
    public static final int DRAWER_POS_ABOUT = 0;

    private String[] drawerItems;
    private DrawerLayout drawerLayout;
    private ListView drawerList;
    private ActionBarDrawerToggle drawerToggle;

    /**
     * Peer list view, contains peers we are connected to.
     */
    private ListView activity_main_log_lv;
    private PeerAdapter adapter;
    /**
     * List of peers we want to display to the user.
     */
    private List<Peer> peers = new ArrayList<>();
    private TextView activity_main_start_tv;
    private ImageView activity_main_logo_iv;
    /**
     * The underlying service that handles connections with peers
     */
    private PeerManagementService peerManagementService;

    /**
     * List view displaying status messages to the user.
     */
    private ListView activity_main_status_lv;
    private StatusAdapter statusAdapter;
    /**
     * List of status messages to display to the user.
     */
    private List<String> statusMessages = new ArrayList<>();

    /**
     * Listens for broadcasts from other parts of the app.
     */
    private BroadcastReceiver localBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String intentAction = intent.getAction();

            if (intentAction.equalsIgnoreCase(PeerManagementService.ACTION_DNS_SEED_DISCOVERY_STARTING)) {
                setStatusUpdate(getString(R.string.activity_main_status_find_seeds_start));
            }

            if (intentAction.equalsIgnoreCase(PeerManagementService.ACTION_DNS_SEED_DISCOVERY_COMPLETE)) {
                int peersFoundFromDnsSeeds = intent.getParcelableArrayListExtra(PeerBroadcaster.KEY_PEERS).size();
                setStatusUpdate(getString(R.string.activity_main_status_find_seeds_complete).replace("{:value}", "" +peersFoundFromDnsSeeds));
            }

            if (intentAction.equalsIgnoreCase(PeerBroadcaster.ACTION_PEER_CONNECTION_ATTEMPT)) {
                String peerAddress = ((Peer)intent.getParcelableExtra(PeerBroadcaster.KEY_PEER)).address;
                setStatusUpdate(getString(R.string.activity_main_status_connect_to_peer).replace("{:value}", peerAddress));
            }

            if (intentAction.equalsIgnoreCase(PeerBroadcaster.ACTION_PEER_CONNECTED)) {
                refreshPeers(peerManagementService.getConnectedPeers());
            }

            if (intentAction.equalsIgnoreCase(PeerBroadcaster.ACTION_PEER_DISCONNECTED)) {
                refreshPeers(peerManagementService.getConnectedPeers());
            }
        }
    };

    /**
     * Allows us to make comms with the PeerManagement Service
     */
    private ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Lawg.i("onServiceConnected");
            PeerManagementService.LocalBinder binder = (PeerManagementService.LocalBinder) iBinder;
            peerManagementService = binder.getService();

            refreshPeers(peerManagementService.getConnectedPeers());
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        constructDrawerMenu();
        setupDrawerUI();

        activity_main_start_tv = findViewById(R.id.activity_main_start_tv);
        activity_main_log_lv = findViewById(R.id.activity_main_log_lv);
        activity_main_status_lv = findViewById(R.id.activity_main_status_lv);
        activity_main_logo_iv = findViewById(R.id.activity_main_logo_iv);

        statusAdapter = new StatusAdapter(this, statusMessages);
        activity_main_status_lv.setAdapter(statusAdapter);

        adapter = new PeerAdapter(this, peers);
        activity_main_log_lv.setAdapter(adapter);

        activity_main_logo_iv.setOnClickListener(this);
        activity_main_log_lv.setOnItemClickListener(this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    private void constructDrawerMenu() {
        drawerItems = new String[DRAWER_MENU_SIZE];
        drawerItems[DRAWER_POS_ABOUT] = getString(R.string.activity_main_drawer_item_about);
    }

    private void setupDrawerUI() {
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerList = (ListView) findViewById(R.id.left_drawer);

        DrawerHeaderView headerView = new DrawerHeaderView(this);
        drawerList.addHeaderView(headerView.getView(), null, false);

        // Set the adapter for the list view
        drawerList.setAdapter(new ArrayAdapter<String>(this,
                R.layout.drawer_list_item, drawerItems));
        // Set the list's click listener
        drawerList.setOnItemClickListener(new DrawerItemClickListener());

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout,
                R.string.activity_main_drawer_open, R.string.activity_main_drawer_close) {

            /* Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /* Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };

        // Set the drawer toggle as the DrawerListener
        drawerLayout.setDrawerListener(drawerToggle);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                drawerLayout,         /* DrawerLayout object */
                R.string.activity_main_drawer_open,  /* "open drawer" description */
                R.string.activity_main_drawer_close  /* "close drawer" description */
        ) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);

            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);

            }
        };

        // Set the drawer toggle as the DrawerListener
        drawerLayout.setDrawerListener(drawerToggle);
    }

    /**
     * Arranges the UI to show the start text and set the circle to the primary color
     * background and show the status listview
     */
    private void showStartButton() {

        activity_main_start_tv.setVisibility(View.VISIBLE);
        activity_main_logo_iv.clearAnimation();
        activity_main_logo_iv.setImageDrawable(getResources().getDrawable(R.drawable.shape_circle_primary));
        activity_main_logo_iv.setVisibility(View.VISIBLE);
        activity_main_status_lv.setVisibility(View.VISIBLE);
    }

    /**
     * Arranges the UI to hide the start text and set the circle background to
     * the app logo and to show the status listview
     */
    private void showLoadingButton() {

        activity_main_start_tv.setVisibility(View.INVISIBLE);
        activity_main_logo_iv.setImageDrawable(getResources().getDrawable(R.mipmap.logo));
        activity_main_logo_iv.setVisibility(View.VISIBLE);
        activity_main_status_lv.setVisibility(View.VISIBLE);
    }

    /**
     * Arranges the UI to hide the start text, circle background and status listview
     */
    private void hideStartAndLoadingButton() {

        activity_main_start_tv.setVisibility(View.INVISIBLE);
        activity_main_logo_iv.clearAnimation();
        activity_main_logo_iv.setVisibility(View.INVISIBLE);
        activity_main_status_lv.setVisibility(View.INVISIBLE);
    }

    /**
     * Updates the peers in the list view.
     * If there are peers to display, we hide other elements on the screen
     * such as the logo etc.
     * @param updatePeers - list of peers to show the user.
     */
    private void refreshPeers(List<Peer> updatePeers) {

        Lawg.i("refreshPeers:" + updatePeers.size());
        if (updatePeers.size() > 0) {

            hideStartAndLoadingButton();
            activity_main_log_lv.setVisibility(View.VISIBLE);
        }

        peers.clear();
        peers.addAll(updatePeers);
        adapter.notifyDataSetChanged();

        if (updatePeers.size() == 0) {
            return;
        }

        GETGeolocationFromIpRequest getGeolocationFromIpRequest = new GETGeolocationFromIpRequest(this, updatePeers.get(0), new Response.Listener<GETGeolocationFromIpResponse>() {
            @Override
            public void onResponse(GETGeolocationFromIpResponse response) {
                Lawg.i("onResponse");
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Lawg.i("onErrorResponse");
            }
        });
        App.getRequestQueue(this).add(getGeolocationFromIpRequest);
    }

    /**
     * Adds a status to let the user know what is going on.
     * If there are peers, we rather show the user that
     * then some stupid status message no one reads anyways...
     * @param newStatus - to show the user.
     */
    private void setStatusUpdate(String newStatus) {

        statusMessages.add(0 , newStatus);
        statusAdapter.notifyDataSetChanged();
    }

    /**
     * Animation to make the red GO button 'shrink' to 0% scale
     */
    private void animateStartButtonShrink() {

        Animation shrink = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.shrink);
        shrink.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {

                showLoadingButton();
                activity_main_logo_iv.clearAnimation();
                animateStartButtonGrow();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
        activity_main_logo_iv.startAnimation(shrink);
    }

    /**
     * Animation to make the logo 'grow' to 100% scale
     */
    private void animateStartButtonGrow() {

        Animation grow = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.grow);
        grow.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {

                activity_main_logo_iv.clearAnimation();
                animateStartButtonPulse();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });

        activity_main_logo_iv.startAnimation(grow);
    }

    /**
     * Animation to make the logo pulsate
     */
    private void animateStartButtonPulse() {

        Animation pulse = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.pulse);
        activity_main_logo_iv.startAnimation(pulse);
    }

    private void startPeerServiceDelayed() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(500);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            startPeerService();
                        }
                    });

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void startPeerService() {

        Intent serviceIntent = new Intent(MainActivity.this, PeerManagementService.class);
        MainActivity.this.startService(serviceIntent);
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.activity_main_logo_iv:
                if (peers.size() <= 0) {
                    startPeerServiceDelayed();
                    animateStartButtonShrink();
                }
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

        Intent intent = new Intent(this, PeerChatActivity.class);
        intent.putExtra(PeerChatActivity.EXTRA_PEER, peers.get(i));
        startActivity(intent);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle your other action bar items...

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();

        LocalBroadcastManager.getInstance(this).unregisterReceiver(localBroadcastReceiver);

        unbindService(serviceConnection);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();

        IntentFilter filter = new IntentFilter();
        filter.addAction(PeerManagementService.ACTION_DNS_SEED_DISCOVERY_STARTING);
        filter.addAction(PeerManagementService.ACTION_DNS_SEED_DISCOVERY_COMPLETE);
        filter.addAction(PeerBroadcaster.ACTION_PEER_CONNECTION_ATTEMPT);
        filter.addAction(PeerBroadcaster.ACTION_PEER_CONNECTED);
        filter.addAction(PeerBroadcaster.ACTION_PEER_DISCONNECTED);
        LocalBroadcastManager.getInstance(this).registerReceiver(localBroadcastReceiver, filter);

        Intent intent = new Intent(this, PeerManagementService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    /* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        boolean drawerOpen = drawerLayout.isDrawerOpen(drawerList);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }
    }

    /**
     * Called when a user clicks on a draw nav menu item
     *
     * @param position
     */
    private void selectItem(int position) {

        // We take into account headers and footers using this integer
        int diff = drawerList.getCount() - DRAWER_MENU_SIZE;

        switch (position - diff) {

            case DRAWER_POS_ABOUT:
                startActivity(new Intent(this, AboutActivity.class));
                break;
        }

        drawerLayout.closeDrawer(drawerList);
    }
}
