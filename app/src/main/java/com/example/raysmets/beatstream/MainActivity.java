package com.example.raysmets.beatstream;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.app.Activity;
import android.view.View;
import android.widget.Toast;


public class MainActivity extends ActionBarActivity {
    private JoinService joinService;

    private BluetoothAdapter myBluetoothAdapter;
    private static final int REQUEST_ENABLE_BT = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        joinService = new JoinService(this, handler);
        myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if(myBluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(), "Your device does not support Bluetooth",
                    Toast.LENGTH_LONG).show();
        }

        //enable bluetooth
        BluetoothOn();

        Intent intent = getIntent();

    }

    //Button calls
    public void HostStream(View view){
        Intent intent = new Intent(this, HostPlaylist.class);
        intent.putExtra("isHost", true);

        startActivity(intent);
    }

    public void JoinStream(View view){
        Intent intent = new Intent(this, DeviceListActivity.class);
        intent.putExtra("isHost", false);
        startActivityForResult(intent, 1);
    }

    private void connectDevice(Intent data, boolean secure) {
        // Get the device MAC address
        String address = data.getExtras()
                .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        // Get the BluetoothDevice object
        BluetoothDevice device = myBluetoothAdapter.getRemoteDevice(address);
        // Attempt to connect to the device
        joinService.connect(device, secure);
    }

    //for testing
    public void setupWifi(View view){
        Intent intent = new Intent(this, SetupWifiActivity.class);
        startActivity(intent);
    }

    public void setupBT(View view){
        Intent intent = new Intent(this, SetupBTActivity.class);
        startActivity(intent);
    }

    public void goToMediaPlayer(View view){
        Intent intent = new Intent(this, MusicPlayer.class);
        startActivity(intent);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
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

    public void BluetoothOn(){
        if (!myBluetoothAdapter.isEnabled()) {
            Toast.makeText(getApplicationContext(), "Bluetooth is required for this app",
                    Toast.LENGTH_LONG).show();
            Intent turnOnIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnOnIntent, REQUEST_ENABLE_BT);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        if(requestCode == REQUEST_ENABLE_BT){
            if(myBluetoothAdapter.isEnabled()) {
                Toast.makeText(getApplicationContext(), "Bluetooth turned on",
                        Toast.LENGTH_LONG).show();
                connectDevice(data, false);

            } else {
                //try again
                BluetoothOn();
            }
        }
    }
}
