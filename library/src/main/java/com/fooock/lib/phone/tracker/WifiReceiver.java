package com.fooock.lib.phone.tracker;

import android.util.Log;

/**
 *
 */
class WifiReceiver implements EnvironmentReceiver<Configuration.Wifi> {
    private static final String TAG = WifiReceiver.class.getSimpleName();

    private Configuration.Wifi wifiConfiguration;

    WifiReceiver(Configuration.Wifi wifiConfiguration) {
        this.wifiConfiguration = wifiConfiguration;
    }

    @Override
    public void register() {
        Log.d(TAG, "Registered wifi receiver...");
    }

    @Override
    public void unregister() {
        Log.d(TAG, "Unregistered wifi receiver...");
    }

    @Override
    public void reloadConfiguration(Configuration.Wifi config) {
        if (wifiConfiguration.equals(config)) {
            Log.i(TAG, "Wifi config is the same, not reload...");
            return;
        }
        Log.d(TAG, "Reloading wifi configuration");
        wifiConfiguration = config;
    }
}
