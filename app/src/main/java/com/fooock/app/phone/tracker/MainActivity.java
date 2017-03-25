package com.fooock.app.phone.tracker;

import android.annotation.TargetApi;
import android.location.Location;
import android.net.wifi.ScanResult;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.telephony.CellInfo;
import android.telephony.NeighboringCellInfo;
import android.util.Log;

import com.fooock.lib.phone.tracker.Configuration;
import com.fooock.lib.phone.tracker.PhoneTracker;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private PhoneTracker phoneTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        phoneTracker = new PhoneTracker(this);

        // Listen for missing permissions
        phoneTracker.addPermissionListener(new PhoneTracker.PermissionListener() {
            @Override
            public void onPermissionNotGranted(String... permission) {
                Log.d(TAG, "Permission not granted: " + Arrays.deepToString(permission));
            }
        });

        // Listen for configuration changes
        phoneTracker.setConfigurationChangeListener(new PhoneTracker.ConfigurationChangeListener() {
            @Override
            public void onConfigurationChange(Configuration configuration) {
                Log.d(TAG, "Tracker configuration changed!");
            }
        });

        // Check the state of the tracker
        boolean running = phoneTracker.isRunning();
        Log.d(TAG, "Running: " + running);

        // Create a new wifi configuration
        Configuration.Wifi wifiConf = new Configuration.Wifi();
        wifiConf.setScanDelay(3000);

        // Create a new cell configuration
        Configuration.Cell cellConf = new Configuration.Cell();
        cellConf.setScanDelay(5000);

        // Create a gps configuration
        Configuration.Gps gpsConf = new Configuration.Gps();
        gpsConf.setMinDistanceUpdate(10);
        gpsConf.setMinTimeUpdate(7000);

        // Create a new configuration
        Configuration configuration = new Configuration.Builder()
                .useCell(true).cell(cellConf)
                .useWifi(true).wifi(wifiConf)
                .useGps(true).gps(gpsConf)
                .create();

        // Set the new init configuration
        phoneTracker.setConfiguration(configuration);

        // Set the listener for cell scans
        phoneTracker.setCellScanListener(new PhoneTracker.CellScanListener() {
            @Override
            @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
            public void onCellInfoReceived(long timestamp, List<CellInfo> cells) {
                Log.d(TAG, "timestamp = [" + timestamp + "], cells = [" + cells.size() + "]");
            }

            @Override
            @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
            public void onNeighborCellReceived(long timestamp, List<NeighboringCellInfo> cells) {
                Log.d(TAG, "timestamp = [" + timestamp + "], cells = [" + cells.size() + "]");
            }
        });

        // Set the listener to receive wifi scans
        phoneTracker.setWifiScanListener(new PhoneTracker.WifiScanListener() {
            @Override
            public void onWifiScansReceived(long timestamp, List<ScanResult> wifiScans) {
                Log.d(TAG, "timestamp = [" + timestamp + "], wifiScans = [" + wifiScans.size() + "]");
            }
        });

        // Set the listener to receive location updates from gps
        phoneTracker.setGpsLocationListener(new PhoneTracker.GpsLocationListener() {
            @Override
            public void onLocationReceived(long timestamp, Location location) {
                Log.d(TAG, "timestamp = [" + timestamp + "], location = [" + location + "]");
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        phoneTracker.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        phoneTracker.stop();
    }
}
