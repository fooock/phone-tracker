package com.fooock.lib.phone.tracker;

/**
 *
 */
public class Configuration {

    private final Builder builder;

    private Configuration(Builder builder) {
        this.builder = builder;
    }

    public boolean usingWifi() {
        return builder.useWifi;
    }

    public boolean usingGps() {
        return builder.useGps;
    }

    public boolean usingCell() {
        return builder.useCell;
    }

    public boolean usingBluetooth() {
        return builder.useBluetooth;
    }

    public static class Builder {

        private boolean useGps = true;
        private boolean useWifi = true;
        private boolean useCell = true;
        private boolean useBluetooth;

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

        public Configuration create() {
            return new Configuration(this);
        }
    }
}
