package com.fooock.lib.phone.tracker;

import android.content.Context;
import android.util.Log;

/**
 *
 */
class WifiReceiver implements EnvironmentReceiver<Configuration.Wifi> {
    private static final String TAG = WifiReceiver.class.getSimpleName();

    private final Context context;
    private final PhoneTracker.WifiScanListener wifiScanListener;

    private Configuration.Wifi wifiConfiguration;

    WifiReceiver(Context context, Configuration.Wifi wifiConfiguration,
                 PhoneTracker.WifiScanListener wifiScanListener) {
        this.context = context;
        this.wifiConfiguration = wifiConfiguration;
        this.wifiScanListener = wifiScanListener;
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
