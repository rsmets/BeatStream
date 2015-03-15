package com.example.raysmets.beatstream;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.lang.reflect.Field;


/**
 * Created by raysmets on 2/26/15.
 */
public class HostPlaylist extends ActionBarActivity {
    private static final String TAG = "HostPlaylist";

    private BluetoothAdapter myBluetoothAdapter;
    private ArrayAdapter<String> SongArrayAdapter;
    private ListView songList;



    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //set the layout of the Activity
        setContentView(R.layout.host_playlist);


        myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        makeDiscoverable();
        JoinService joinService = new JoinService(this);
        joinService.start();

        //generate playlist
        songList = (ListView) findViewById(R.id.songList);
        SongArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        songList.setAdapter(SongArrayAdapter);
        songList.setOnItemClickListener(
                new AdapterView.OnItemClickListener(){

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        playSong(String.valueOf(parent.getItemAtPosition(position)));

                    }
                }
        );

        populateList();


    }

    public void makeDiscoverable() {
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
    private void populateList(){

        //metadata retriever
        MediaMetadataRetriever metaData = new MediaMetadataRetriever();


        Field[] songs = com.example.raysmets.beatstream.R.raw.class.getFields();
        int count = 0;
        for (Field f:songs) {
            count = count +1;
            try {
                int resID = getResources().getIdentifier(f.getName(),
                        "raw", getPackageName());

                final AssetFileDescriptor afd=getResources().openRawResourceFd(resID);

                metaData.setDataSource(afd.getFileDescriptor(),afd.getStartOffset(),afd.getLength());


                SongArrayAdapter.add(metaData.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
                        + "\n" + metaData.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST));
                SongArrayAdapter.notifyDataSetChanged();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (count == 0) {
            SongArrayAdapter.add("No Songs Found");
            SongArrayAdapter.notifyDataSetChanged();
            songList.setEnabled(false);
        }
    }

    private void playSong(String songName) {

        //test
        Toast.makeText(getApplicationContext(), songName,
                Toast.LENGTH_LONG).show();
        //test
    }
}