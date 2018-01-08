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

import android.os.Build;

/**
 * Check Android version
 */
class CheckVersion {

    /**
     * Check the given android version code in the current device
     *
     * @param version android version code to check
     * @return true if the device is the specified version, false if not
     */
    boolean isEqualTo(final int version) {
        return Build.VERSION.SDK_INT == version;
    }

    /**
     * Check if the given version code is equal or greater than the device version
     *
     * @param version android version code to check
     * @return true if the device version is equal or greater, false if not
     */
    boolean isEqualOrGreater(final int version) {
        return Build.VERSION.SDK_INT >= version;
    }
}
