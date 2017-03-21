package com.fooock.lib.phone.tracker;

import android.util.Log;

/**
 *
 */
class GpsReceiver implements EnvironmentReceiver<Configuration.Gps> {
    private static final String TAG = GpsReceiver.class.getSimpleName();

    private Configuration.Gps gpsConfiguration;

    GpsReceiver(Configuration.Gps gpsConfiguration) {
        this.gpsConfiguration = gpsConfiguration;
    }

    @Override
    public void register() {
        Log.d(TAG, "Registered gps receiver...");
    }

    @Override
    public void unregister() {
        Log.d(TAG, "Unregistered gps receiver...");
    }

    @Override
    public void reloadConfiguration(Configuration.Gps config) {
        if (gpsConfiguration.equals(config)) {
            Log.i(TAG, "Gps config is the same, not reload...");
            return;
        }
        Log.d(TAG, "Reloading gps configuration");
        gpsConfiguration = config;
    }
}
