package com.fooock.lib.phone.tracker;

/**
 *
 */
interface EnvironmentReceiver<T> {

    void register();

    void unregister();

    void reloadConfiguration(T config);
}
