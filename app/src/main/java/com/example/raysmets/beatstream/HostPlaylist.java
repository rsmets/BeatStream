package com.example.raysmets.beatstream;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;

/**
 * Created by raysmets on 2/26/15.
 */
public class HostPlaylist extends ActionBarActivity {
    private static final String TAG = "HostPlaylist";

    private BluetoothAdapter myBluetoothAdapter;

    /**
     * The Handler that gets information back from the JoinService
     */
    public final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            }
        }
    };


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //set the layout of the Activity
        setContentView(R.layout.setup_wifi);
        myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        makeDiscoverable();
        JoinService joinService = new JoinService(this, handler);
        joinService.start();

    }

    public void makeDiscoverable(){
        /**
         * Makes this device discoverable.
         */

        if (myBluetoothAdapter.getScanMode() !=
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 120);
            Log.d(TAG, "making discoverable for 120 seconds");
            startActivity(discoverableIntent);
        }

    }

    /**
     * Start device discover with the BluetoothAdapter
     */
    private void doDiscovery() {
        Log.d(TAG, "doDiscovery()");

        // Indicate scanning in the title
        setProgressBarIndeterminateVisibility(true);
        setTitle(R.string.scanning);

        // Turn on sub-title for new devices
        //findViewById(R.id.title_new_devices).setVisibility(View.VISIBLE);

        // If we're already discovering, stop it
        if (myBluetoothAdapter.isDiscovering()) {
            myBluetoothAdapter.cancelDiscovery();
        }

        // Request discover from BluetoothAdapter
        myBluetoothAdapter.startDiscovery();
    }
}
