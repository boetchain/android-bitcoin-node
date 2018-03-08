package com.boetchain.bitcoinnode.worker.location;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.support.v4.content.ContextCompat;

import com.boetchain.bitcoinnode.util.Lawg;

/**
 * This class gets the users location once without updating until
 * asked to.
 *
 * Created by Tyler on 2014-07-26.
 */
public class UserCoarseLocation {

    private Context context;
    private LocationManager locMan;
    private Location location;

    private CoarseLocation coarseLocation;

    private boolean running;
    private boolean onThread;

    private UserLocationListener userLocationCallBack;

    public UserCoarseLocation(Context context) {

        this.context = context;
        locMan = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        running = false;
    }

    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location loc) {
        }
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}
        @Override
        public void onProviderEnabled(String provider) {}
        @Override
        public void onProviderDisabled(String provider) {}
    };

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public void setUserLocationListener(UserLocationListener cb) {

        userLocationCallBack = cb;
    }

    /**
     * Starts a CoarseLocation thread to get the user's location.
     * @param onThread - true if this method should run on it's own thread
     */
    public void retrieveCoarseLocation(boolean onThread) {

        this.onThread = onThread;
        if (coarseLocation != null && isRunning()) {
            coarseLocation.interrupt();
        }
        coarseLocation = new CoarseLocation();

        if (onThread) {
            coarseLocation.start();
        } else {
            coarseLocation.run();
        }
    }

    public boolean isRunning() {
        return running;
    }

    class CoarseLocation extends Thread
    {

        @Override
        public void run() {

            running = true;

            if (ContextCompat.checkSelfPermission(context,
                                                  Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && locMan != null
                && locMan.getProvider(LocationManager.NETWORK_PROVIDER) != null) {

                String provider;

                provider = locMan.getProvider(LocationManager.NETWORK_PROVIDER).getName();

                if (Looper.myLooper() == null) {
                    Looper.prepare();
                }

                int count = 5;
                for (int i = 0; i < count; i++) {

                	Lawg.i("asdf get location");
                    locMan.requestSingleUpdate(provider, locationListener, Looper.myLooper());
                    location = locMan.getLastKnownLocation(provider);

                    if (location != null) {
                        break;
                    } else {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            onPostExecute();
        }

        protected void onPostExecute() {

            if (userLocationCallBack != null) {
                userLocationCallBack.locationRetrieved(location);
            }

            running = false;
        }
    }
}
