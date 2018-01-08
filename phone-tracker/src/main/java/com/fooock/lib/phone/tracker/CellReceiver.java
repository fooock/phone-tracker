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

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.telephony.CellInfo;
import android.telephony.NeighboringCellInfo;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.util.Collections;
import java.util.List;

/**
 *
 */
class CellReceiver implements EnvironmentReceiver<Configuration.Cell> {
    private static final String TAG = CellReceiver.class.getSimpleName();

    private final TelephonyManager telephonyManager;
    private final CheckVersion checkVersion = new CheckVersion();
    private final CheckPermission checkPermission;
    private final Handler handler = new Handler();
    private final PhoneTracker.CellScanListener cellScanListener;

    private Configuration.Cell cellConfiguration;

    CellReceiver(Context context, Configuration.Cell cellConfiguration,
                 PhoneTracker.CellScanListener cellScanListener) {
        this.telephonyManager = (TelephonyManager) context.getSystemService(
                Context.TELEPHONY_SERVICE);
        this.cellConfiguration = cellConfiguration;
        this.checkPermission = new CheckPermission(context);
        this.cellScanListener = cellScanListener;
    }

    @Override
    public void register() {
        Log.d(TAG, "Registered cell receiver...");

        // Get the android version to execute the cell scanning
        boolean equalOrGreater17 = checkVersion.isEqualOrGreater(
                Build.VERSION_CODES.JELLY_BEAN_MR1);
        if (equalOrGreater17) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    final boolean permEnabled = checkPermission.hasAnyPermission(
                            PhoneTracker.LOCATION_PERMISSIONS);

                    final int scanDelay = cellConfiguration.getScanDelay();

                    if (!permEnabled) {
                        Log.w(TAG, "Location permissions not granted to cell scan, trying again in "
                                + scanDelay + "ms");
                        handler.postDelayed(this, scanDelay);
                        return;
                    }

                    final List<CellInfo> cellInfo = telephonyManager.getAllCellInfo();
                    final long timestamp = System.currentTimeMillis();
                    if (cellInfo == null || cellInfo.isEmpty()) {
                        if (cellScanListener != null) {
                            cellScanListener.onCellInfoReceived(
                                    timestamp, Collections.<CellInfo>emptyList());
                        }
                    } else {
                        if (cellScanListener != null) {
                            cellScanListener.onCellInfoReceived(timestamp, cellInfo);
                        }
                    }

                    Log.d(TAG, "Scanning cell every " + scanDelay + "ms");
                    handler.postDelayed(this, scanDelay);
                }
            });
        } else {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    final boolean permEnabled = checkPermission.hasAnyPermission(
                            PhoneTracker.LOCATION_PERMISSIONS);

                    final int scanDelay = cellConfiguration.getScanDelay();

                    if (!permEnabled) {
                        Log.w(TAG, "Location permissions not granted to cell scan, trying again in "
                                + scanDelay + "ms");
                        handler.postDelayed(this, scanDelay);
                        return;
                    }

                    final List<NeighboringCellInfo> cellInfo
                            = telephonyManager.getNeighboringCellInfo();
                    final long timestamp = System.currentTimeMillis();
                    if (cellInfo == null || cellInfo.isEmpty()) {
                        cellScanListener.onNeighborCellReceived(
                                timestamp, Collections.<NeighboringCellInfo>emptyList());
                    } else {
                        cellScanListener.onNeighborCellReceived(timestamp, cellInfo);
                    }

                    Log.d(TAG, "Scanning cell every " + scanDelay + "ms");
                    handler.postDelayed(this, scanDelay);
                }
            });
        }
    }

    @Override
    public void unregister() {
        Log.d(TAG, "Unregistered cell receiver...");
        handler.removeCallbacksAndMessages(null);
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
