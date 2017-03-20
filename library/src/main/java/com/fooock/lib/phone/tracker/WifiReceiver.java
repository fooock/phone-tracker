package com.fooock.lib.phone.tracker;

import android.util.Log;

/**
 *
 */
class WifiReceiver implements EnvironmentReceiver {
    private static final String TAG = WifiReceiver.class.getSimpleName();

    @Override
    public void register() {
        Log.d(TAG, "Registered wifi receiver...");
    }

    @Override
    public void unregister() {
        Log.d(TAG, "Unregistered wifi receiver...");
    }
}
