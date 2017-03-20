package com.fooock.lib.phone.tracker;

import android.Manifest;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 *
 */
public class PhoneTracker {
    private static final String TAG = PhoneTracker.class.getSimpleName();

    private static final String[] LOCATION_PERMISSIONS = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    private static final String[] WIFI_PERMISSIONS = new String[] {
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_STATE
    };

    private static final String[] BLUETOOTH_PERMISSIONS = new String[] {
            Manifest.permission.BLUETOOTH
    };

    private final Context context;
    private final List<PermissionListener> permissionListeners = new ArrayList<>();
    private final CheckVersion checkVersion = new CheckVersion();
    private final CheckPermission checkPermission;
    private final Object lock = new Object();

    private boolean running;

    private Configuration configuration;

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
    public void setConfiguration(Configuration configuration) {
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
}
