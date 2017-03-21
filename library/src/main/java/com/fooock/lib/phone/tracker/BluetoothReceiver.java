package com.fooock.lib.phone.tracker;

import android.util.Log;

/**
 *
 */
class BluetoothReceiver implements EnvironmentReceiver<Configuration.Bluetooth> {
    private static final String TAG = BluetoothReceiver.class.getSimpleName();

    private Configuration.Bluetooth bluetoothConfiguration;

    BluetoothReceiver(Configuration.Bluetooth bluetoothConfiguration) {
        this.bluetoothConfiguration = bluetoothConfiguration;
    }

    @Override
    public void register() {
        Log.d(TAG, "Registered bluetooth receiver...");
    }

    @Override
    public void unregister() {
        Log.d(TAG, "Unregistered bluetooth receiver...");
    }

    @Override
    public void reloadConfiguration(Configuration.Bluetooth config) {
        if (bluetoothConfiguration.equals(config)) {
            Log.i(TAG, "Bluetooth config is the same, not reload...");
            return;
        }
        Log.d(TAG, "Reloading bluetooth configuration");
        bluetoothConfiguration = config;
    }
}
