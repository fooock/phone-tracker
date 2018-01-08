/*
 * Copyright (c) 2018. newhouse (nhitbh at gmail dot com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.fooock.lib.phone.tracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.util.Log;

import java.util.List;

/**
 *
 */
class WifiReceiver implements EnvironmentReceiver<Configuration.Wifi> {
    private static final String TAG = WifiReceiver.class.getSimpleName();

    private final Context context;
    private final PhoneTracker.WifiScanListener wifiScanListener;
    private final Handler handler = new Handler();
    private final CheckVersion checkVersion = new CheckVersion();
    private final CheckPermission checkPermission;
    private final WifiManager wifiManager;

    private Configuration.Wifi wifiConfiguration;

    /**
     * Wifi broadcast receiver
     */
    private final BroadcastReceiver wifiReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (wifiScanListener == null) {
                return;
            }
            final List<ScanResult> scanResults = wifiManager.getScanResults();
            final long timestamp = System.currentTimeMillis();
            wifiScanListener.onWifiScansReceived(timestamp, scanResults);
        }
    };

    WifiReceiver(Context context, Configuration.Wifi wifiConfiguration,
                 PhoneTracker.WifiScanListener wifiScanListener) {
        this.context = context;
        this.wifiConfiguration = wifiConfiguration;
        this.wifiScanListener = wifiScanListener;
        this.checkPermission = new CheckPermission(context);
        this.wifiManager = (WifiManager) context.getApplicationContext()
                .getSystemService(Context.WIFI_SERVICE);
    }

    @Override
    public void register() {
        Log.d(TAG, "Registered wifi receiver...");

        // register receiver
        context.registerReceiver(wifiReceiver, new IntentFilter(
                WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

        handler.post(new Runnable() {
            @Override
            public void run() {
                final boolean androidMOrGreater = checkVersion.isEqualOrGreater(
                        Build.VERSION_CODES.M);

                final boolean wifiEnabled = checkPermission.hasPermissions(
                        PhoneTracker.WIFI_PERMISSIONS);
                final boolean locationEnabled = checkPermission.hasAnyPermission(
                        PhoneTracker.LOCATION_PERMISSIONS);

                if (androidMOrGreater && !locationEnabled) {
                    Log.w(TAG, "Location permissions not granted to scan wifi in android >= 6.0");
                    handler.postDelayed(this, wifiConfiguration.getScanDelay());
                    return;
                }
                if (wifiEnabled) {
                    Log.d(TAG, "Scanning wifi every "
                            + wifiConfiguration.getScanDelay() + "ms");
                    wifiManager.startScan();
                }
                handler.postDelayed(this, wifiConfiguration.getScanDelay());
            }
        });
    }

    @Override
    public void unregister() {
        Log.d(TAG, "Unregistered wifi receiver...");
        handler.removeCallbacksAndMessages(null);
        context.unregisterReceiver(wifiReceiver);
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
