package com.fooock.lib.phone.tracker;

import android.util.Log;

/**
 *
 */
class CellReceiver implements EnvironmentReceiver {
    private static final String TAG = CellReceiver.class.getSimpleName();

    @Override
    public void register() {
        Log.d(TAG, "Registered cell receiver...");
    }

    @Override
    public void unregister() {
        Log.d(TAG, "Unregistered cell receiver...");
    }
}
