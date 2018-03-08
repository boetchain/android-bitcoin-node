package com.boetchain.bitcoinnode.ui.activity;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.view.MenuItem;

import com.boetchain.bitcoinnode.R;
import com.boetchain.bitcoinnode.model.Peer;
import com.boetchain.bitcoinnode.util.Constants;
import com.boetchain.bitcoinnode.util.DeviceUtil;
import com.boetchain.bitcoinnode.util.Notify;
import com.boetchain.bitcoinnode.util.PermissionUtil;
import com.boetchain.bitcoinnode.worker.broadcaster.PeerBroadcaster;
import com.boetchain.bitcoinnode.worker.location.UserCoarseLocation;
import com.boetchain.bitcoinnode.worker.location.UserLocationListener;
import com.boetchain.bitcoinnode.worker.service.PeerManagementService;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

public class MapActivity extends BaseActivity implements OnMapReadyCallback, UserLocationListener {

	private GoogleMap googleMap;
	private UserCoarseLocation userCoarseLocation;
	private boolean firstLocationRetrieved = false;

	/**
	 * The underlying service that handles connections with peers
	 */
	private PeerManagementService peerManagementService;

	private List<PeerPair> peerPairs = new ArrayList<>();

	/**
	 * Allows us to make comms with the PeerManagement Service
	 */
	private ServiceConnection serviceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName componentName, IBinder iBinder) {

			PeerManagementService.LocalBinder binder = (PeerManagementService.LocalBinder) iBinder;
			peerManagementService = binder.getService();
		}

		@Override
		public void onServiceDisconnected(ComponentName componentName) {

		}
	};

	/**
	 * Listens for broadcasts from other parts of the app.
	 */
	private BroadcastReceiver localBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String intentAction = intent.getAction();

			if (intentAction.equalsIgnoreCase(PeerManagementService.ACTION_DNS_SEED_DISCOVERY_STARTING)) {
				//tell the user we're looking for peers
			}

			if (intentAction.equalsIgnoreCase(PeerManagementService.ACTION_DNS_SEED_DISCOVERY_COMPLETE)) {
				//tell the user we found peers
			}

			if (intentAction.equalsIgnoreCase(PeerBroadcaster.ACTION_PEER_CONNECTION_ATTEMPT)) {
				//tell the user (if we have no peers) that we are connecting to a peer
			}

			if (intentAction.equalsIgnoreCase(PeerBroadcaster.ACTION_PEER_CONNECTED) && peerManagementService != null) {
				Peer peer = intent.getParcelableExtra(PeerBroadcaster.KEY_PEER);
				addPeer(peer);
			}

			if (intentAction.equalsIgnoreCase(PeerBroadcaster.ACTION_PEER_DISCONNECTED) && peerManagementService != null) {
				Peer peer = intent.getParcelableExtra(PeerBroadcaster.KEY_PEER);
				removePeer(peer);
			}

			if (intentAction.equalsIgnoreCase(PeerManagementService.ACTION_SERVICE_STARTED)) {
				//tell user we're looking for peers
			}

			if (intentAction.equalsIgnoreCase(PeerManagementService.ACTION_SERVICE_DESTROYED)) {
				//tell the user the service was destroyed
				removeAllPeers();

			}

			if (intentAction.equalsIgnoreCase(PeerBroadcaster.ACTION_PEER_UPDATED)) {
				Peer peer = intent.getParcelableExtra(PeerBroadcaster.KEY_PEER);
				removePeer(peer);
				addPeer(peer);
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map);

		if (getSupportActionBar() != null) {
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		}

		// Obtain the SupportMapFragment and get notified when the googleMap is ready to be used.
		SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.activity_map_google_map);
		mapFragment.getMapAsync(this);

		userCoarseLocation = new UserCoarseLocation(this);
		userCoarseLocation.setUserLocationListener(this);
	}

	private void bindPeerService() {

		Intent intent = new Intent(this, PeerManagementService.class);
		bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
	}

	private void unbindPeerService() {

		unbindService(serviceConnection);
	}

	/**
	 * Draws a bitmap for the user. This bitmap will be ready to be set on the googleMap
	 * @return
	 */
	private Bitmap drawUserMarker() {

		//create the user marker bitmap
		int px = getResources().getDimensionPixelSize(R.dimen.map_user_marker_size);
		Bitmap userMarkerBitmap = Bitmap.createBitmap(px, px, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(userMarkerBitmap);
		Drawable shape;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			shape = getDrawable(R.mipmap.ic_launcher);
		} else {
			shape = getResources().getDrawable(R.mipmap.ic_launcher);
		}
		shape.setBounds(0, 0, userMarkerBitmap.getWidth(), userMarkerBitmap.getHeight());
		shape.draw(canvas);

		return userMarkerBitmap;
	}

	/**
	 * Creates a GoogleMap marker from a bitmap and draws it to the googleMap.
	 * @return
	 */
	private Marker makeUserMarker() {

		LatLng latLng = new LatLng(userCoarseLocation.getLocation().getLatitude(),
		                           userCoarseLocation.getLocation().getLongitude());

		MarkerOptions markerOptions = new MarkerOptions()
				.position(latLng)
				.anchor(0.5f, 0.5f)
				.icon(BitmapDescriptorFactory.fromBitmap(drawUserMarker()));

		return googleMap.addMarker(markerOptions);
	}

	/**
	 * Creates a GoogleMap marker from a bitmap and draws it to the googleMap.
	 * @param peer
	 * @return
	 */
	private Marker makePeerMarker(Peer peer) {

		LatLng latLng = new LatLng(peer.lat,
		                           peer.lng);

		MarkerOptions markerOptions = new MarkerOptions()
				.position(latLng)
				.anchor(0.5f, 0.5f)
				.icon(BitmapDescriptorFactory.fromBitmap(drawPeerMarker()));

		return googleMap.addMarker(markerOptions);
	}

	/**
	 * Draws a bitmap for the specified peer. This bitmap will be ready to be set on the googleMap
	 * @return
	 */
	private Bitmap drawPeerMarker() {

		//create the user marker bitmap
		int px = getResources().getDimensionPixelSize(R.dimen.map_peer_marker_size);
		Bitmap userMarkerBitmap = Bitmap.createBitmap(px, px, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(userMarkerBitmap);
		Drawable shape;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			shape = getDrawable(R.drawable.ic_peer_default);
		} else {
			shape = getResources().getDrawable(R.drawable.ic_peer_default);
		}
		shape.setBounds(0, 0, userMarkerBitmap.getWidth(), userMarkerBitmap.getHeight());
		shape.draw(canvas);

		return userMarkerBitmap;
	}

	/**
	 * Adds the specified peer to the peerPairs array and the googleMap
	 */
	private void addPeer(Peer peer) {

		PeerPair peerPair = new PeerPair(peer, makePeerMarker(peer));
		peerPairs.add(peerPair);
	}

	/**
	 * Removes the specified peer from the peerPairs array and the googleMap
	 */
	private void removePeer(Peer peer) {

		for (PeerPair peerPair : peerPairs) {
			if (peerPair.peer.address.equals(peer.address)) {
				peerPair.marker.remove();
				peerPairs.remove(peerPair);
				break;
			}
		}
	}

	/**
	 * Animates the googleMap camera to the specified latLng
	 * @param latLng
	 */
	public void moveCamera(final LatLng latLng) {

		final float mapZoom = Constants.MAP.ZOOM_DEFAULT;

		new Handler().post(new Runnable() {
			@Override
			public void run() {

				if (latLng != null) {
					CameraPosition cameraPosition = new CameraPosition.Builder()
							.target(latLng)
							.zoom(mapZoom)
							.bearing(0)
							.tilt(0)
							.build();
					googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition),
					                        Constants.ANIM.DURATION_FAST,
					                        null);
				}
			}
		});
	}

	/**
	 * Loops through all connected peers and adds them to the peerPair list and googleMap.
	 */
	private void addPeersToMap() {

		peerPairs.clear();
		List<Peer> peers = peerManagementService.getConnectedPeers();

		for (Peer peer : peers) {
			addPeer(peer);
		}
	}

	/**
	 * Removes all peers from peerPairs and peer markers from googleMap
	 */
	private void removeAllPeers() {

		for (PeerPair peerPair : peerPairs) {
			peerPair.marker.remove();
		}

		peerPairs.clear();
	}

	/**
	 * Clears all the peer markers from the map and sets them again
	 */
	private void resetPeerMarkers() {

		for (PeerPair peerPair : peerPairs) {
			peerPair.marker.remove();
			peerPair.marker = makePeerMarker(peerPair.peer);
		}
	}

	@Override
	protected void onPostResume() {
		super.onPostResume();

		if (PermissionUtil.requestLocationPermission(this)) {
			return;
		}

		boolean locationOn = DeviceUtil.isNetworkLocationOn(this);

		if (!locationOn) {
			Notify.askLocationDialog(this).setCancelable(false);
		} else if (!DeviceUtil.isInternetConnection(this)) {
			Notify.alertDialog(this, "", R.string.error_no_internet);
		} else if (!firstLocationRetrieved) {
			userCoarseLocation.retrieveCoarseLocation(true);
		}

		bindPeerService();

		IntentFilter filter = new IntentFilter();
		filter.addAction(PeerManagementService.ACTION_DNS_SEED_DISCOVERY_STARTING);
		filter.addAction(PeerManagementService.ACTION_DNS_SEED_DISCOVERY_COMPLETE);
		filter.addAction(PeerManagementService.ACTION_SERVICE_STARTED);
		filter.addAction(PeerManagementService.ACTION_SERVICE_DESTROYED);
		filter.addAction(PeerBroadcaster.ACTION_PEER_CONNECTION_ATTEMPT);
		filter.addAction(PeerBroadcaster.ACTION_PEER_CONNECTED);
		filter.addAction(PeerBroadcaster.ACTION_PEER_UPDATED);
		filter.addAction(PeerBroadcaster.ACTION_PEER_DISCONNECTED);
		LocalBroadcastManager.getInstance(this).registerReceiver(localBroadcastReceiver, filter);

		resetPeerMarkers();
	}

	@Override protected void onPause() {
		super.onPause();

		unbindPeerService();
	}

	/**
	 * Manipulates the googleMap once available.
	 * This callback is triggered when the googleMap is ready to be used.
	 * This is where we can add markers or lines, add listeners or move the camera. In this case,
	 * we just add a marker near Sydney, Australia.
	 * If Google Play services is not installed on the device, the user will be prompted to install
	 * it inside the SupportMapFragment. This method will only be triggered once the user has
	 * installed Google Play services and returned to the app.
	 */
	@Override
	public void onMapReady(GoogleMap googleMap) {
		this.googleMap = googleMap;

		UiSettings settings = this.googleMap.getUiSettings();
		settings.setCompassEnabled(false);
		settings.setMyLocationButtonEnabled(false);
		settings.setZoomControlsEnabled(false);
		settings.setMapToolbarEnabled(false);
		settings.setTiltGesturesEnabled(false);
		settings.setRotateGesturesEnabled(false);

		this.googleMap.setMapStyle(
				MapStyleOptions.loadRawResourceStyle(
						this, R.raw.map_style));

		userCoarseLocation.retrieveCoarseLocation(true);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
			case android.R.id.home:
				finish();
				return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public void locationRetrieved(final Location location) {

		runOnUiThread(new Runnable() {

			@Override
			public void run() {

				if (location != null) {
					firstLocationRetrieved = true;
					makeUserMarker();
				}

				addPeersToMap();
			}
		});
	}

	/**
	 * A map marker and peer pair that allows us to easily add or remove peers from the map
	 *
	 */
	private class PeerPair {

		Peer peer;
		Marker marker;

		private PeerPair(Peer peer, Marker marker) {
			this.peer = peer;
			this.marker = marker;
		}
	}
}
