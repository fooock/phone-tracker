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
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

/**
 *
 */
class GpsReceiver implements EnvironmentReceiver<Configuration.Gps> {
    private static final String TAG = GpsReceiver.class.getSimpleName();

    private final PhoneTracker.GpsLocationListener gpsLocationListener;
    private final LocationManager locationManager;
    private final CheckLocationProvider checkLocationProvider;

    private Configuration.Gps gpsConfiguration;

    /**
     * Listener for location updates
     */
    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            Log.d(TAG, "Min distance: " + gpsConfiguration.getMinDistanceUpdate()
                    + " Min time: " + gpsConfiguration.getMinTimeUpdate());

            if (gpsLocationListener == null) {
                return;
            }
            final long timestamp = System.currentTimeMillis();
            gpsLocationListener.onLocationReceived(timestamp, location);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }
    };

    GpsReceiver(Context context, Configuration.Gps gpsConfiguration,
                PhoneTracker.GpsLocationListener gpsLocationListener) {
        this.gpsConfiguration = gpsConfiguration;
        this.gpsLocationListener = gpsLocationListener;
        this.locationManager = (LocationManager) context.getSystemService(
                Context.LOCATION_SERVICE);
        this.checkLocationProvider = new CheckLocationProvider(locationManager);
    }

    @Override
    public void register() {
        Log.d(TAG, "Registered gps receiver...");

        // check if any provider is enabled
        final boolean providerEnabled = checkLocationProvider.isEnabled();
        if (providerEnabled) {
            boolean gpsProviderEnabled = checkLocationProvider.gpsProviderEnabled();

            registerProvider(gpsProviderEnabled ? LocationManager.GPS_PROVIDER :
                    LocationManager.NETWORK_PROVIDER);
        } else {
            Log.w(TAG, "No location providers enabled");
        }
    }

    private void registerProvider(String provider) {
        locationManager.requestLocationUpdates(provider, gpsConfiguration.getMinTimeUpdate(),
                gpsConfiguration.getMinDistanceUpdate(), locationListener);
    }

    @Override
    public void unregister() {
        Log.d(TAG, "Unregistered gps receiver...");
        locationManager.removeUpdates(locationListener);
    }

    @Override
    public void reloadConfiguration(Configuration.Gps config) {
        if (gpsConfiguration.equals(config)) {
            Log.i(TAG, "Gps config is the same, not reload...");
            return;
        }
        Log.d(TAG, "Reloading gps configuration");
        gpsConfiguration = config;

        // to reload the gps configuration we need to unregister and register again
        // the gps sensor
        unregister();
        register();
    }

    /**
     * Class to check if any location provider is enabled for the device
     */
    private static class CheckLocationProvider {
        private final LocationManager locationManager;

        CheckLocationProvider(LocationManager locationManager) {
            this.locationManager = locationManager;
        }

        /**
         * Check if the {@link LocationManager#GPS_PROVIDER} or the
         * {@link LocationManager#NETWORK_PROVIDER} are enabled for this device
         *
         * @return True if any of the two providers are enabled, false if not
         */
        private boolean isEnabled() {
            return gpsProviderEnabled() || networkProviderEnabled();
        }

        private boolean gpsProviderEnabled() {
            return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        }

        private boolean networkProviderEnabled() {
            return locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        }
    }
}
