package com.example.raysmets.beatstream;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import java.util.ArrayList;


/**
 * Created by raysmets on 2/26/15.
 */
public class HostPlaylist extends ActionBarActivity {
    private static final String TAG = "HostPlaylist";

    private BluetoothAdapter myBluetoothAdapter;
    private ArrayAdapter<String> SongArrayAdapter;
    private ListView songList;
    private ArrayList<String> songFiles = new ArrayList<String>();
    JoinService joinService;

    /*public HostPlaylist(JoinService js){
        joinService = js;
    }*/



    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //set the layout of the Activity
        setContentView(R.layout.host_playlist);

        joinService = MyApplication.getJoinService();


        myBluetoothAdapter = MyApplication.getBTadapter();

        makeDiscoverable();

        joinService.start();

        //generate playlist
        songList = (ListView) findViewById(R.id.songList);
        SongArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        songList.setAdapter(SongArrayAdapter);
        songList.setOnItemClickListener(
                new AdapterView.OnItemClickListener(){

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        playSong(songFiles.get(position));

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
                songFiles.add(f.getName());


                int resID = getResources().getIdentifier(f.getName(),
                        "raw", getPackageName());

                final AssetFileDescriptor afd=getResources().openRawResourceFd(resID);

                metaData.setDataSource(afd.getFileDescriptor(),afd.getStartOffset(),afd.getLength());

                int songDurationS = Integer.valueOf(metaData.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)) / 1000;
                int songDurationM = songDurationS/60;
                songDurationS = songDurationS%60;

                String songDuration = String.valueOf(songDurationM) + ":";
                if (songDurationS<10) {
                    songDuration = songDuration + "0" + String.valueOf(songDurationS);
                }
                else {
                    songDuration = songDuration + String.valueOf(songDurationS);
                }


                SongArrayAdapter.add(metaData.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
                        + "   [" + songDuration + "]"
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

        //Song Metadata for player
        int resID = getResources().getIdentifier(songName,
                "raw", getPackageName());

        final AssetFileDescriptor afd=getResources().openRawResourceFd(resID);

        MediaMetadataRetriever metaData = new MediaMetadataRetriever();
        metaData.setDataSource(afd.getFileDescriptor(),afd.getStartOffset(),afd.getLength());


        String songTitle = metaData.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
        String songArtist = metaData.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
        int songDurationMS = Integer.valueOf(metaData.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
        int songDurationS = songDurationMS / 1000;
        int songDurationM = songDurationS/60;
        songDurationS = songDurationS%60;

        String songDuration = String.valueOf(songDurationM) + ":";
        if (songDurationS<10) {
            songDuration = songDuration + "0" + String.valueOf(songDurationS);
        }
        else {
            songDuration = songDuration + String.valueOf(songDurationS);
        }

        //album artwork
        byte[] albumbytes = metaData.getEmbeddedPicture();


        //Music Player Intent
        Intent intent = new Intent(this, MusicPlayer.class);
        intent.putExtra("fileName", songName);
        intent.putExtra("songTitle", songTitle);
        intent.putExtra("songArtist", songArtist);
        intent.putExtra("songDurationMS", songDurationMS);
        intent.putExtra("songDuration", songDuration);
        intent.putExtra("albumCover", albumbytes);
        startActivity(intent);


    }
}