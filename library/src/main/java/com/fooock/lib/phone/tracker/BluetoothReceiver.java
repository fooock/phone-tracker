package com.fooock.lib.phone.tracker;

import android.util.Log;

/**
 *
 */
class BluetoothReceiver implements EnvironmentReceiver {
    private static final String TAG = BluetoothReceiver.class.getSimpleName();

    @Override
    public void register() {
        Log.d(TAG, "Registered bluetooth receiver...");
    }

    @Override
    public void unregister() {
        Log.d(TAG, "Unregistered bluetooth receiver...");
    }
}
