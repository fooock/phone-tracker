package com.fooock.lib.phone.tracker;

import android.Manifest;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
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
    private static final String[] LOCATION_PERMISSIONS = new String[]{
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
            wifiReceiver = new WifiReceiver(configuration.wifiConfiguration());
            wifiReceiver.register();
        }
        if (usingCell) {
            cellReceiver = new CellReceiver(configuration.cellConfiguration());
            cellReceiver.register();
        }
        if (usingGps) {
            gpsReceiver = new GpsReceiver(configuration.gpsConfiguration());
            gpsReceiver.register();
        }
        if (usingBluetooth) {
            bluetoothReceiver = new BluetoothReceiver(configuration.bluetoothConfiguration());
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
                Log.w(TAG, "Not running, can't stop...");
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
            wifiReceiver = new WifiReceiver(conf.wifiConfiguration());
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
            gpsReceiver = new GpsReceiver(conf.gpsConfiguration());
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
            cellReceiver = new CellReceiver(conf.cellConfiguration());
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
            bluetoothReceiver = new BluetoothReceiver(conf.bluetoothConfiguration());
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
}
