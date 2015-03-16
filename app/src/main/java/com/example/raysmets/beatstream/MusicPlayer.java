package com.example.raysmets.beatstream;

import android.content.Context;
import android.os.Looper;
import android.os.Message;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.ActionBarActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import android.app.Activity;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import org.apache.commons.io.IOUtils;

/**
 * Created by raysmets on 2/24/15.
 */
public class MusicPlayer extends ActionBarActivity {

    private static final String TAG = "Musicplayer";

    private MediaPlayer mediaPlayer;
    private BlockingQueue<byte[]> bytesQ;
    public JoinService joinService;
    public TextView songName, duration;
    public ImageView AlbumCoverImage;
    private double timeElapsed = 0, finalTime = 0;
    private int forwardTime = 2000, backwardTime = 2000;
    //private Handler durationHandler = new Handler();
    private SeekBar seekbar;
    private String FileName;
    private int songID;
    private String songTitle;
    private String songArtist;
    private int songDurationMS;
    private String songDuration;
    private Bitmap albumCover;
    public playBytesThread playThread;
    Context context;

    public MusicPlayer(BlockingQueue<byte[]> bytes) {
        bytesQ = bytes;
        playThread = new playBytesThread(bytesQ, this);
        playThread.start();

    }

    public void add(byte[] bytes){
        if(bytesQ == null)
            Log.d(TAG, "bytesQ is NULLLLLLLLLLLLL!!!!!");
        bytesQ.add(bytes);
    }

    public MusicPlayer(){

    }

    /*public MusicPlayer(){

    }*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getApplicationContext();
        //set the layout of the Activity
        setContentView(R.layout.music_player);

        Intent intent = getIntent();
        FileName = intent.getStringExtra("fileName");
        songTitle = intent.getStringExtra("songTitle");
        songArtist = intent.getStringExtra("songArtist");
        songDurationMS = intent.getIntExtra("songDurationMS", 1);
        songDuration = intent.getStringExtra("songDuration");
        joinService = MyApplication.getJoinService();

        byte[] albumbytes;
        albumbytes = intent.getByteArrayExtra("albumCover");
        try{
            albumCover = BitmapFactory.decodeByteArray(albumbytes, 0, albumbytes.length);
        }
        catch(NullPointerException e)
        {
            //no album cover
        }

        songID = getResources().getIdentifier(FileName,
                "raw", getPackageName());



        //initialize views
        initializeViews();



    }

    public void initializeViews(){
        songName = (TextView) findViewById(R.id.songName);
        mediaPlayer = MediaPlayer.create(this, songID);
        finalTime = mediaPlayer.getDuration();
        duration = (TextView) findViewById(R.id.songDuration);
        seekbar = (SeekBar) findViewById(R.id.seekBar);

        AlbumCoverImage = (ImageView) findViewById(R.id.mp3Image);
        if (albumCover!=null){
            AlbumCoverImage.setImageBitmap (albumCover);
        }
        songName.setText(songTitle);

        seekbar.setMax((int) finalTime);
        seekbar.setClickable(false);
    }

    private void sendMusic(int sample_song) throws IOException {
        Context contx = MyApplication.getAppContext();
        if(contx == null)
            Log.d(TAG, "context is null@#$@#$#@$%@%^#$%^&$%&%^&$^");
        byte[] payload = IOUtils.toByteArray(contx.getResources().openRawResource(R.raw.sample_song));
        Log.i(TAG, "sending the byte array to joinService");
        joinService.write(payload);
    }

    public void playbytes(byte[] bytes){
        try {
            Log.d(TAG, "in playbytes method!!!!!");
            // create temp file that will hold byte array
            File tempMp3 = File.createTempFile("kurchina", "mp3", getCacheDir());
            tempMp3.deleteOnExit();
            FileOutputStream fos = new FileOutputStream(tempMp3);
            fos.write(bytes);
            fos.close();

            // Tried reusing instance of media player
            // but that resulted in system crashes...
            mediaPlayer.reset();

            // Tried passing path directly, but kept getting
            // "Prepare failed.: status=0x1"
            // so using file descriptor instead
            FileInputStream fis = new FileInputStream(tempMp3);
            mediaPlayer.setDataSource(fis.getFD());
            Log.i(TAG, "playing recieved bytes**********");
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException ex) {
            String s = ex.toString();
            ex.printStackTrace();
        }
    }

    // play mp3 song
    public void play(View view) {
        try {
            Log.i(TAG, "beginning the process of sending audio");
            sendMusic(R.raw.sample_song);
        } catch (IOException e) {
            e.printStackTrace();
        }

        mediaPlayer.start();
        timeElapsed = mediaPlayer.getCurrentPosition();
        seekbar.setProgress((int) timeElapsed);
        //durationHandler.postDelayed(updateSeekBarTime, 100);
    }

    //handler to change seekBarTime
    private Runnable updateSeekBarTime = new Runnable() {
        public void run() {
            //get current position
            timeElapsed = mediaPlayer.getCurrentPosition();
            //set seekbar progress
            seekbar.setProgress((int) timeElapsed);
            //set time remaing
            double timeRemaining = finalTime - timeElapsed;
            duration.setText(String.format("%d min, %d sec", TimeUnit.MILLISECONDS.toMinutes((long) timeRemaining), TimeUnit.MILLISECONDS.toSeconds((long) timeRemaining) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) timeRemaining))));

            //repeat yourself that again in 100 miliseconds
            //durationHandler.postDelayed(this, 100);
        }
    };

    // pause mp3 song
    public void pause(View view) {
        mediaPlayer.pause();
    }

    // go forward at forwardTime seconds
    public void forward(View view) {
        //check if we can go forward at forwardTime seconds before song endes
        if ((timeElapsed + forwardTime) <= finalTime) {
            timeElapsed = timeElapsed + forwardTime;

            //seek to the exact second of the track
            mediaPlayer.seekTo((int) timeElapsed);
        }
    }

    private class playBytesThread extends Thread{
        public BlockingQueue<byte[]> playQ;
        public MusicPlayer musicPlayer;

        public playBytesThread(BlockingQueue<byte[]> bytes, MusicPlayer mp){
            musicPlayer = mp;
            playQ = bytes;
        }

        public void run(){
            Log.i(TAG, "starting playBytesThread");
            Looper.prepare();
            byte[] bytes = new byte[1024];
            while(true){
                try {
                    Log.d(TAG, "trying to grab bytes in playBytesThread");
                    bytes = playQ.take();
                    if(bytes != null) Log.d(TAG,"successfully grabbed bytes in playBytesThread");
                    musicPlayer.playbytes(bytes);
                } catch (InterruptedException e) {
                    Log.d(TAG, "can't grab audio bytes");
                    e.printStackTrace();
                }

            }
        }
    }

}
