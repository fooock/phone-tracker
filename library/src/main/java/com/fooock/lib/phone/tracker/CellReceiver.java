package com.fooock.lib.phone.tracker;

import android.util.Log;

/**
 *
 */
class CellReceiver implements EnvironmentReceiver<Configuration.Cell> {
    private static final String TAG = CellReceiver.class.getSimpleName();

    private Configuration.Cell cellConfiguration;

    CellReceiver(Configuration.Cell cellConfiguration) {
        this.cellConfiguration = cellConfiguration;
    }

    @Override
    public void register() {
        Log.d(TAG, "Registered cell receiver...");
    }

    @Override
    public void unregister() {
        Log.d(TAG, "Unregistered cell receiver...");
    }

    @Override
    public void reloadConfiguration(Configuration.Cell config) {
        if (cellConfiguration.equals(config)) {
            Log.i(TAG, "Cell config is the same, not reload...");
            return;
        }
        Log.d(TAG, "Reloading cell configuration");
        cellConfiguration = config;
    }
}
