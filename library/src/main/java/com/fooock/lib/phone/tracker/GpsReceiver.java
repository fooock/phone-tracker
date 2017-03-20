package com.fooock.lib.phone.tracker;

import android.util.Log;

/**
 *
 */
class GpsReceiver implements EnvironmentReceiver {
    private static final String TAG = GpsReceiver.class.getSimpleName();

    @Override
    public void register() {
        Log.d(TAG, "Registered gps receiver...");
    }

    @Override
    public void unregister() {
        Log.d(TAG, "Unregistered gps receiver...");
    }
}
