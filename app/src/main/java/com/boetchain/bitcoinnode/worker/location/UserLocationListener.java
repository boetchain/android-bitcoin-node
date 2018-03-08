package com.boetchain.bitcoinnode.worker.location;

import android.location.Location;

/**
 * Created by Tyler on 2015-10-24.
 */
public interface UserLocationListener {

    public void locationRetrieved(Location location);
}
