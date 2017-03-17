package com.fooock.app.phone.tracker;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.fooock.lib.phone.tracker.Configuration;
import com.fooock.lib.phone.tracker.PhoneTracker;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private PhoneTracker phoneTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        phoneTracker = new PhoneTracker(this);

        // Listen for missing permissions
        phoneTracker.addPermissionListener(new PhoneTracker.PermissionListener() {
            @Override
            public void onPermissionNotGranted(String... permission) {
                Log.d(TAG, "Permission not granted: " + Arrays.deepToString(permission));
            }
        });

        // Create a new configuration
        Configuration configuration = new Configuration.Builder()
                .create();
        phoneTracker.setConfiguration(configuration);
    }

    @Override
    protected void onResume() {
        super.onResume();
        phoneTracker.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        phoneTracker.stop();
    }
}
