package com.boetchain.bitcoinnode.ui.activity;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;

import com.boetchain.bitcoinnode.R;
import com.boetchain.bitcoinnode.util.Constants;
import com.boetchain.bitcoinnode.util.DeviceUtil;
import com.boetchain.bitcoinnode.util.Lawg;
import com.boetchain.bitcoinnode.util.Notify;
import com.boetchain.bitcoinnode.util.PermissionUtil;
import com.boetchain.bitcoinnode.worker.location.UserCoarseLocation;
import com.boetchain.bitcoinnode.worker.location.UserLocationListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapActivity extends BaseActivity implements OnMapReadyCallback, UserLocationListener {

	private GoogleMap googleMap;
	private UserCoarseLocation userCoarseLocation;
	private boolean firstLocationRetrieved = false;
//TODO ask for location permission in onResume
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

	private void setUserMarker() {

		LatLng latLng = new LatLng(userCoarseLocation.getLocation().getLatitude(),
		                           userCoarseLocation.getLocation().getLongitude());

		MarkerOptions markerOptions = new MarkerOptions()
				.position(latLng)
				.anchor(0.5f, 0.5f)
				.icon(BitmapDescriptorFactory.fromBitmap(drawUserMarker()));

		googleMap.addMarker(markerOptions);
	}

	public void moveCamera(final LatLng latLng, final int bottomPadding) {

		final float mapZoom = Constants.MAP.ZOOM_DEFAULT;

		new Handler().post(new Runnable() {
			@Override
			public void run() {

				if (bottomPadding >= 0) {
					googleMap.setPadding(0, 0, 0, bottomPadding);
				}

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

	@Override
	protected void onPostResume() {
		super.onPostResume();

		//todo when permission granted, make sure this get's the user's location
		if (PermissionUtil.requestLocationPermission(this)) {
			return;
		}

		boolean locationOn = DeviceUtil.isNetworkLocationOn(this);

		Lawg.i("asdf location on: " + locationOn);

		if (!locationOn) {
			Notify.askLocationDialog(this).setCancelable(false);
		} else if (!DeviceUtil.isInternetConnection(this)) {
			Notify.alertDialog(this, "", R.string.error_no_internet);
		} else if (!firstLocationRetrieved) {
			userCoarseLocation.retrieveCoarseLocation(true);
		}
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

		Lawg.i("asdf got location: " + (location == null));
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				if (location != null) {
					firstLocationRetrieved = true;
					setUserMarker();
					moveCamera(new LatLng(location.getLatitude(), location.getLongitude()), -1);
				}
			}
		});
	}
}
