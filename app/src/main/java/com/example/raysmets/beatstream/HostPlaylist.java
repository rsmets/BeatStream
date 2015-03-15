package com.example.raysmets.beatstream;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;

/**
 * Created by raysmets on 2/26/15.
 */
public class HostPlaylist extends ActionBarActivity {
    private static final String TAG = "HostPlaylist";

    private BluetoothAdapter myBluetoothAdapter;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //set the layout of the Activity
        setContentView(R.layout.setup_wifi);
        myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        makeDiscoverable();


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
}
