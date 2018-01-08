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
import android.content.pm.PackageManager;
import android.os.Process;

/**
 * Check for permission in the device
 */
class CheckPermission {

    private final Context context;

    /**
     * Creates this object
     *
     * @param context Application context
     */
    CheckPermission(Context context) {
        this.context = context;
    }

    /**
     * Check if the given permission is granted for this process
     *
     * @param permission permission to check
     * @return true if the permission is granted, false if not
     */
    boolean isEnabled(final String permission) {
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
    boolean hasAnyPermission(final String... permissions) {
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
    boolean hasPermissions(final String... permissions) {
        boolean hasAllPerms = true;
        for (String permission : permissions) {
            if (!isEnabled(permission)) {
                hasAllPerms = false;
            }
        }
        return hasAllPerms;
    }
}
