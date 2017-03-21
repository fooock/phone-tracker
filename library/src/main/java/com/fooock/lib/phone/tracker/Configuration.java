package com.fooock.lib.phone.tracker;

import android.support.annotation.NonNull;

/**
 * Class to configure what sensors we use and how this sensors scan the environment
 */
public class Configuration {

    private final Builder builder;

    private Configuration(Builder builder) {
        this.builder = builder;
    }

    /**
     * @return True if using wifi scan, false if not
     */
    public boolean usingWifi() {
        return builder.useWifi;
    }

    /**
     * @return True if using gps location, false if not
     */
    public boolean usingGps() {
        return builder.useGps;
    }

    /**
     * @return True if using cell scanning, false if not
     */
    public boolean usingCell() {
        return builder.useCell;
    }

    /**
     * @return True if using bluetooth scanning, false if not
     */
    public boolean usingBluetooth() {
        return builder.useBluetooth;
    }

    /**
     * @return The Wifi configuration
     */
    public Wifi wifiConfiguration() {
        return builder.wifiConfiguration;
    }

    /**
     * @return The cell configuration
     */
    public Cell cellConfiguration() {
        return builder.cellConfiguration;
    }

    /**
     * @return The gps configuration
     */
    public Gps gpsConfiguration() {
        return builder.gpsConfiguration;
    }

    /**
     * @return The bluetooth configuration
     */
    public Bluetooth bluetoothConfiguration() {
        return builder.bluetoothConfiguration;
    }

    /**
     * Builder class to create the configuration
     */
    public static class Builder {

        private boolean useGps = true;
        private boolean useWifi = true;
        private boolean useCell = true;
        private boolean useBluetooth;

        private Wifi wifiConfiguration = new Wifi();
        private Cell cellConfiguration = new Cell();
        private Gps gpsConfiguration = new Gps();
        private Bluetooth bluetoothConfiguration = new Bluetooth();

        public Builder useGps(boolean useGps) {
            this.useGps = useGps;
            return this;
        }

        public Builder useWifi(boolean useWifi) {
            this.useWifi = useWifi;
            return this;
        }

        public Builder useCell(boolean useCell) {
            this.useCell = useCell;
            return this;
        }

        public Builder useBluetooth(boolean useBluetooth) {
            this.useBluetooth = useBluetooth;
            return this;
        }

        public Builder wifi(@NonNull Wifi wifiConf) {
            this.wifiConfiguration = wifiConf;
            return this;
        }

        public Builder cell(@NonNull Cell cellConf) {
            this.cellConfiguration = cellConf;
            return this;
        }

        public Builder gps(@NonNull Gps gpsConf) {
            this.gpsConfiguration = gpsConf;
            return this;
        }

        public Builder bluetooth(@NonNull Bluetooth bluetoothConf) {
            this.bluetoothConfiguration = bluetoothConf;
            return this;
        }

        /**
         * This method create the configuration
         *
         * @return Configuration
         */
        @NonNull
        public Configuration create() {
            return new Configuration(this);
        }
    }

    /**
     * Wifi configuration
     */
    public static class Wifi {
        private static final int SCAN_DEFAULT_DELAY = 4000;

        private int scanDelay = SCAN_DEFAULT_DELAY;

        public int getScanDelay() {
            return scanDelay;
        }

        public void setScanDelay(int scanDelay) {
            this.scanDelay = scanDelay;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Wifi wifi = (Wifi) o;

            return scanDelay == wifi.scanDelay;

        }

        @Override
        public int hashCode() {
            return scanDelay;
        }
    }

    /**
     *
     */
    public static class Cell {

    }

    /**
     *
     */
    public static class Gps {

    }

    /**
     *
     */
    public static class Bluetooth {

    }
}
