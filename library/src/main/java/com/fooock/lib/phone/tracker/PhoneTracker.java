package com.fooock.lib.phone.tracker;

import android.Manifest;
import android.content.Context;
import android.location.Location;
import android.net.wifi.ScanResult;
import android.os.Build;
import android.support.annotation.NonNull;
import android.telephony.CellInfo;
import android.telephony.NeighboringCellInfo;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class PhoneTracker {
    private static final String TAG = PhoneTracker.class.getSimpleName();

    /**
     * Permissions used for gps and cell location. Note that for android >= 6 this permissions are
     * needed for scan wifi and bluetooth
     */
    static final String[] LOCATION_PERMISSIONS = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    /**
     * Permissions used to scan wifi AP's
     */
    private static final String[] WIFI_PERMISSIONS = new String[]{
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_STATE
    };

    /**
     * Permissions used to scan for bluetooth
     */
    private static final String[] BLUETOOTH_PERMISSIONS = new String[]{
            Manifest.permission.BLUETOOTH
    };

    private final Context context;
    private final List<PermissionListener> permissionListeners = new ArrayList<>();
    private final CheckVersion checkVersion = new CheckVersion();
    private final CheckPermission checkPermission;
    private final Object lock = new Object();

    private boolean running;

    private WifiReceiver wifiReceiver;
    private CellReceiver cellReceiver;
    private GpsReceiver gpsReceiver;
    private BluetoothReceiver bluetoothReceiver;
    private Configuration configuration;

    private ConfigurationChangeListener configurationChangeListener;
    private CellScanListener cellScanListener;
    private WifiScanListener wifiScanListener;
    private GpsLocationListener gpsLocationListener;
    private BluetoothScanListener bluetoothScanListener;

    /**
     * Listener to notify missing permissions
     */
    public interface PermissionListener {
        /**
         * Called when a required permission is not granted
         *
         * @param permission Names of the permissions
         */
        void onPermissionNotGranted(String... permission);
    }

    /**
     * Listener for tracker configuration changes. Changes on configuration are triggered when
     * the method {@link #updateConfiguration(Configuration)} is called
     */
    public interface ConfigurationChangeListener {
        /**
         * Method called when the configuration change
         *
         * @param configuration New configuration
         */
        void onConfigurationChange(Configuration configuration);
    }

    /**
     * Listener to receive cell scans. Note that only one of the two methods of this
     * interface can be called. This is because the method {@link #onCellInfoReceived(long, List)}
     * is only called when the android version is greater than or equal to
     * {@code android.os.Build.VERSION_CODES.JELLY_BEAN_MR1}, and the
     * {@link #onNeighborCellReceived(long, List)} method only is called when the android version
     * is minor to {@code android.os.Build.VERSION_CODES.JELLY_BEAN_MR1}
     */
    public interface CellScanListener {
        /**
         * Called when the cell scan is completed. This method only is called in android
         * versions greater than or equal to {@code android.os.Build.VERSION_CODES.JELLY_BEAN_MR1}
         *
         * @param timestamp Current time in milliseconds when the scans are received
         * @param cells     List of scanned cells, never null
         */
        void onCellInfoReceived(long timestamp, List<CellInfo> cells);

        /**
         * Called when the cell scan is completed. This method only is called in android
         * versions minor to {@code android.os.Build.VERSION_CODES.JELLY_BEAN_MR1}
         *
         * @param timestamp Current time in milliseconds when the scans are received
         * @param cells     List of scanned cells, never null
         */
        void onNeighborCellReceived(long timestamp, List<NeighboringCellInfo> cells);
    }

    /**
     * Adapter class for {@link CellScanListener}
     */
    public static abstract class CellScanAdapter implements CellScanListener {
        @Override
        public void onCellInfoReceived(long timestamp, List<CellInfo> cells) {
        }

        @Override
        public void onNeighborCellReceived(long timestamp, List<NeighboringCellInfo> cells) {
        }
    }

    /**
     * Listener to receive wifi scans
     */
    public interface WifiScanListener {
        /**
         * Called when the wifi scan is completed
         *
         * @param timestamp Current time in milliseconds when the wifi scans are received
         * @param wifiScans List of wifi scans. Never null
         */
        void onWifiScansReceived(long timestamp, List<ScanResult> wifiScans);
    }

    /**
     * Listener to receive location updates from the gps
     */
    public interface GpsLocationListener {
        /**
         * Called when the location update is received
         *
         * @param timestamp Current time in milliseconds when the location is received
         * @param location  Current device location
         */
        void onLocationReceived(long timestamp, Location location);
    }

    /**
     * Listener to receive bluetooth low energy scans
     */
    public interface BluetoothScanListener {
        /**
         * Called when the bluetooth low energy scans are received
         *
         * @param timestamp   Current time in milliseconds when the scans are received
         * @param scanResults List of bluetooth scans. Never null
         */
        void onBluetoothScanReceived(long timestamp, List<android.bluetooth.le.ScanResult> scanResults);
    }

    /**
     * Create the phone tracker
     *
     * @param context Application context
     */
    public PhoneTracker(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("Context can't be null");
        }
        this.context = context;
        this.checkPermission = new CheckPermission(context);
    }

    /**
     * Start the phone tracker
     */
    public void start() {
        // If is running do nothing...
        synchronized (lock) {
            if (running) {
                Log.d(TAG, "Tracker is running now...");
                return;
            }
        }
        // If configuration is null then use the default configuration
        if (configuration == null) {
            configuration = new Configuration.Builder().create();
        }

        final boolean equalOrGreaterM = checkVersion.isEqualOrGreater(Build.VERSION_CODES.M);

        final boolean usingWifi = configuration.usingWifi();
        final boolean usingGps = configuration.usingGps();
        final boolean usingBluetooth = configuration.usingBluetooth();
        final boolean usingCell = configuration.usingCell();

        // Check for wifi scan permissions
        if (usingWifi) {
            // android m or greater need location permissions for scan wifi
            if (equalOrGreaterM
                    && !checkPermission.hasAnyPermission(LOCATION_PERMISSIONS)
                    && !checkPermission.hasPermissions(WIFI_PERMISSIONS)) {
                notifyPermissionsNotGranted(LOCATION_PERMISSIONS);
                return;
            }
        }
        // Check for gps permissions
        if (usingGps) {
            if (equalOrGreaterM && !checkPermission.hasAnyPermission(LOCATION_PERMISSIONS)) {
                notifyPermissionsNotGranted(LOCATION_PERMISSIONS);
                return;
            }
        }
        // Check for bluetooth scan permissions
        if (usingBluetooth) {
            if (equalOrGreaterM
                    && !checkPermission.hasAnyPermission(LOCATION_PERMISSIONS)
                    && !checkPermission.hasPermissions(BLUETOOTH_PERMISSIONS)) {
                notifyPermissionsNotGranted(LOCATION_PERMISSIONS);
                return;
            }
        }
        // Check for cell scan permissions
        if (usingCell) {
            if (equalOrGreaterM && !checkPermission.hasAnyPermission(LOCATION_PERMISSIONS)) {
                notifyPermissionsNotGranted(LOCATION_PERMISSIONS);
                return;
            }
        }

        if (usingWifi) {
            wifiReceiver = new WifiReceiver(
                    context, configuration.wifiConfiguration(), wifiScanListener);
            wifiReceiver.register();
        }
        if (usingCell) {
            cellReceiver = new CellReceiver(
                    context, configuration.cellConfiguration(), cellScanListener);
            cellReceiver.register();
        }
        if (usingGps) {
            gpsReceiver = new GpsReceiver(
                    context, configuration.gpsConfiguration(), gpsLocationListener);
            gpsReceiver.register();
        }
        if (usingBluetooth) {
            bluetoothReceiver = new BluetoothReceiver(
                    context, configuration.bluetoothConfiguration(), bluetoothScanListener);
            bluetoothReceiver.register();
        }
        synchronized (lock) {
            running = true;
        }
        Log.d(TAG, "Starting now...");
    }

    /**
     * Stop the phone tracker
     */
    public void stop() {
        synchronized (lock) {
            if (!running) {
                Log.w(TAG, "Not running, can't stop ;-)");
                return;
            }
        }
        if (wifiReceiver != null) {
            wifiReceiver.unregister();
        }
        if (cellReceiver != null) {
            cellReceiver.unregister();
        }
        if (gpsReceiver != null) {
            gpsReceiver.unregister();
        }
        if (bluetoothReceiver != null) {
            bluetoothReceiver.unregister();
        }
        removePermissionListener();
        Log.d(TAG, "Stopped tracker");

        synchronized (lock) {
            running = false;
        }
    }

    /**
     * Add a new {@link PermissionListener}
     *
     * @param listener Permission listener
     */
    public void addPermissionListener(PermissionListener listener) {
        synchronized (permissionListeners) {
            permissionListeners.add(listener);
        }
    }

    /**
     * Remove the permissions listener when not needed
     */
    private void removePermissionListener() {
        synchronized (permissionListeners) {
            permissionListeners.clear();
        }
    }

    /**
     * Set a new {@link Configuration}
     *
     * @param configuration Configuration
     */
    public void setConfiguration(@NonNull Configuration configuration) {
        this.configuration = configuration;
    }

    /**
     * Method to notify missing permissions to the permission listener
     *
     * @param permissions Permissions
     */
    private void notifyPermissionsNotGranted(String... permissions) {
        if (permissionListeners.isEmpty()) {
            return;
        }
        for (PermissionListener permissionListener : permissionListeners) {
            permissionListener.onPermissionNotGranted(permissions);
        }
    }

    /**
     * Check if the tracker is running or not
     *
     * @return True if running, false if not
     */
    public boolean isRunning() {
        synchronized (lock) {
            return running;
        }
    }

    /**
     * Update the current configuration. If the tracker is not running this method only set
     * the new configuration using the {@link #setConfiguration(Configuration)} method.
     *
     * @param conf Configuration
     */
    public void updateConfiguration(@NonNull Configuration conf) {
        if (!isRunning()) {
            setConfiguration(configuration);
            return;
        }
        // If the old config is not using the wifi but the new config yes, then start
        // the wifi
        if (!configuration.usingWifi() && conf.usingWifi()) {
            wifiReceiver = new WifiReceiver(context, conf.wifiConfiguration(), wifiScanListener);
            wifiReceiver.register();

            // Unregister the wifi receiver if not needed more
        } else if (configuration.usingWifi() && !conf.usingWifi()) {
            wifiReceiver.unregister();
            wifiReceiver = null;

            // Reload wifi configuration
        } else if (configuration.usingWifi() && conf.usingWifi()) {
            wifiReceiver.reloadConfiguration(conf.wifiConfiguration());
        }

        // If the old config is not using the gps but the new config yes, then start
        // the gps
        if (!configuration.usingGps() && conf.usingGps()) {
            gpsReceiver = new GpsReceiver(context, conf.gpsConfiguration(), gpsLocationListener);
            gpsReceiver.register();

            // Unregister the gps receiver if not needed more
        } else if (configuration.usingGps() && !conf.usingGps()) {
            gpsReceiver.unregister();
            gpsReceiver = null;

            // Reload gps configuration
        } else if (configuration.usingGps() && conf.usingGps()) {
            gpsReceiver.reloadConfiguration(conf.gpsConfiguration());
        }

        // If the old config is not using the cell but the new config yes, then start
        // the cell
        if (!configuration.usingCell() && conf.usingCell()) {
            cellReceiver = new CellReceiver(context, conf.cellConfiguration(), cellScanListener);
            cellReceiver.register();

            // Unregister the cell receiver if not needed more
        } else if (configuration.usingCell() && !conf.usingCell()) {
            cellReceiver.unregister();
            cellReceiver = null;

            // Reload cell configuration
        } else if (configuration.usingCell() && conf.usingCell()) {
            cellReceiver.reloadConfiguration(conf.cellConfiguration());
        }

        // If the old config is not using the bluetooth but the new config yes, then start
        // the bluetooth
        if (!configuration.usingBluetooth() && conf.usingBluetooth()) {
            bluetoothReceiver = new BluetoothReceiver(
                    context, conf.bluetoothConfiguration(), bluetoothScanListener);
            bluetoothReceiver.register();

            // Unregister the bluetooth receiver if not needed more
        } else if (configuration.usingBluetooth() && !conf.usingBluetooth()) {
            bluetoothReceiver.unregister();
            bluetoothReceiver = null;

            // Reload bluetooth configuration
        } else if (configuration.usingBluetooth() && conf.usingBluetooth()) {
            bluetoothReceiver.reloadConfiguration(conf.bluetoothConfiguration());
        }

        // Change the configuration
        setConfiguration(conf);

        if (configurationChangeListener == null) {
            return;
        }
        configurationChangeListener.onConfigurationChange(conf);
    }

    /**
     * Set the listener for configuration changes
     *
     * @param listener Listener
     */
    public void setConfigurationChangeListener(ConfigurationChangeListener listener) {
        this.configurationChangeListener = listener;
    }

    /**
     * Set the listener to receive the cell scans
     *
     * @param cellScanListener Cell scan listener
     */
    public void setCellScanListener(CellScanListener cellScanListener) {
        this.cellScanListener = cellScanListener;
    }

    /**
     * Set the listener to receive wifi scans
     *
     * @param wifiScanListener Wifi scan listener
     */
    public void setWifiScanListener(WifiScanListener wifiScanListener) {
        this.wifiScanListener = wifiScanListener;
    }

    /**
     * Set the listener to receive location updates
     *
     * @param gpsLocationListener Gps location listener
     */
    public void setGpsLocationListener(GpsLocationListener gpsLocationListener) {
        this.gpsLocationListener = gpsLocationListener;
    }

    /**
     * Set the listener to receive bluetooth scans
     *
     * @param bluetoothScanListener Bluetooth scan listener
     */
    public void setBluetoothScanListener(BluetoothScanListener bluetoothScanListener) {
        this.bluetoothScanListener = bluetoothScanListener;
    }
}
