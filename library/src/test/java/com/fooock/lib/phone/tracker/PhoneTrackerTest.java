package com.fooock.lib.phone.tracker;

import org.junit.Test;

/**
 *
 */
public class PhoneTrackerTest {

    @Test(expected = IllegalArgumentException.class)
    public void testNullContext() throws Exception {
        new PhoneTracker(null);
    }
}