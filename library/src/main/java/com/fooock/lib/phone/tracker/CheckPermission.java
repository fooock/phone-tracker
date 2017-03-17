package com.fooock.lib.phone.tracker;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Process;

/**
 * Check for permission in the device
 */
public class CheckPermission {

    private final Context context;

    /**
     * Creates this object
     *
     * @param context Application context
     */
    public CheckPermission(Context context) {
        this.context = context;
    }

    /**
     * Check if the given permission is granted for this process
     *
     * @param permission permission to check
     * @return true if the permission is granted, false if not
     */
    public boolean isEnabled(final String permission) {
        final int callingPermission = context.checkPermission(permission,
                Process.myPid(), Process.myUid());
        return callingPermission == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Check if any of the given permissions are granted
     *
     * @param permissions Permissions to check
     * @return True if any of the permissions are granted, false otherwise
     */
    public boolean hasAnyPermission(final String... permissions) {
        boolean hasPermission = false;
        for (String permission : permissions) {
            if (isEnabled(permission)) {
                hasPermission = true;
            }
        }
        return hasPermission;
    }

    /**
     * Check if all of the given permissions in the current process are granted
     *
     * @param permissions permissions to check
     * @return true if all permissions are enabled, false otherwise
     */
    public boolean hasPermissions(final String... permissions) {
        boolean hasAllPerms = true;
        for (String permission : permissions) {
            if (!isEnabled(permission)) {
                hasAllPerms = false;
            }
        }
        return hasAllPerms;
    }
}
