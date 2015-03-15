package com.example.raysmets.beatstream;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import java.util.concurrent.TimeUnit;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

/**
 * Created by raysmets on 2/24/15.
 */
public class MusicPlayer extends ActionBarActivity {

    static MediaPlayer mediaPlayer = null;
    public static TextView songName, duration;
    public ImageView AlbumCoverImage;
    private static double timeElapsed = 0, finalTime = 0;
    private static int forwardTime = 2000, backwardTime = 2000;
    private static Handler durationHandler = new Handler();
    private static SeekBar seekbar;
    SharedPreferences prefs;
    static boolean Playing;
    private String FileName;
    private int songID;
    private String songTitle;
    private String songArtist;
    private int songDurationMS;
    private String songDuration;
    private Bitmap albumCover;
    byte[] albumbytes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //set the layout of the Activity
        setContentView(R.layout.music_player);

        Intent intent = getIntent();
        FileName = intent.getStringExtra("fileName");
        songTitle = intent.getStringExtra("songTitle");
        songArtist = intent.getStringExtra("songArtist");
        songDurationMS = intent.getIntExtra("songDurationMS", 1);
        songDuration = intent.getStringExtra("songDuration");
        albumbytes = intent.getByteArrayExtra("albumCover");
        try{
            albumCover = BitmapFactory.decodeByteArray(albumbytes, 0, albumbytes.length);
        }
        catch(NullPointerException e)
        {
            //no album cover
        }

        SharedPreferences settings = getSharedPreferences("1",0);
        int length = settings.getInt("TheOffset",0);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        songID = getResources().getIdentifier(FileName,
                "raw", getPackageName());

        //initialize views
        if(mediaPlayer == null) {
            initializeViews();
        }
    }

    @Override
    protected void onPause(){
        super.onPause();
        duration = (TextView) findViewById(R.id.songDuration);
        SharedPreferences.Editor prefsEdit = prefs.edit();
        boolean isPlaying = prefs.getBoolean("mediaplaying",false);
        if(isPlaying) {
            if (mediaPlayer != null && !Playing) {
                mediaPlayer.pause();
            }
            int position = mediaPlayer.getCurrentPosition();
            prefsEdit.putInt("mediaPosition", position);
            prefsEdit.commit();
        }
        System.out.println("Paused");
        durationHandler.postDelayed(updateSeekBarTime, 100);
    }

    @Override
    protected void onResume(){
        super.onResume();
        duration = (TextView) findViewById(R.id.songDuration);
        AlbumCoverImage = (ImageView) findViewById(R.id.mp3Image);
        if (albumCover!=null){
            AlbumCoverImage.setImageBitmap (albumCover);
        }
        songName = (TextView) findViewById(R.id.songName);
        songName.setText(songTitle);
        durationHandler.postDelayed(updateSeekBarTime, 100);
        if (mediaPlayer == null) {
               mediaPlayer = MediaPlayer.create(this, R.raw.sample_song);
        }
        if(Playing) {
            mediaPlayer.start();
        }
        boolean isPlaying = prefs.getBoolean("mediaPlaying",false);
        if(isPlaying){
            int position = prefs.getInt("mediaPosition",0);
            mediaPlayer.seekTo(position);
        }
        System.out.println("Resumed");


    }

    public void initializeViews(){
        songName = (TextView) findViewById(R.id.songName);
        if(mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(this, songID);
        }
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
        Playing = false;
    }

    // play mp3 song
    public void play(View view) {
        Playing = true;
        if(!mediaPlayer.isPlaying()) {
            playMusic();
        }
        durationHandler.postDelayed(updateSeekBarTime, 100);
    }

    //handler to change seekBarTime
    private Runnable updateSeekBarTime = new Runnable() {
        public void run() {
            seekbar = (SeekBar) findViewById(R.id.seekBar);
            seekbar.setMax((int) finalTime);
            seekbar.setClickable(false);
            //get current position
            timeElapsed = mediaPlayer.getCurrentPosition();
            //set seekbar progress
            seekbar.setProgress((int) timeElapsed);
            //set time remaing
            double timeRemaining = finalTime - timeElapsed;
            duration.setText(String.format("%d min, %d sec", TimeUnit.MILLISECONDS.toMinutes((long) timeRemaining), TimeUnit.MILLISECONDS.toSeconds((long) timeRemaining) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) timeRemaining))));
            //repeat yourself that again in 100 miliseconds
            durationHandler.postDelayed(this, 100);
        }
    };

    // pause mp3 song
    public void pause(View view) {
        if(mediaPlayer == null){
            mediaPlayer = MediaPlayer.create(this, R.raw.sample_song);
            mediaPlayer.pause();
        }


        Playing = false;
        SharedPreferences.Editor prefsEdit = prefs.edit();
        boolean isPlaying = prefs.getBoolean("mediaplaying",false);
        if(isPlaying) {
            if (mediaPlayer != null) {
                mediaPlayer.pause();
            }
            int position = mediaPlayer.getCurrentPosition();
            prefsEdit.putInt("mediaPosition", position);
            prefsEdit.commit();
        }
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

    private void playMusic(){
        httpGetAsynchTask httpGetAsynchTask = new httpGetAsynchTask();
        httpGetAsynchTask.execute();
    }

    class httpGetAsynchTask extends AsyncTask <String, Integer, Void>{
        @Override
        protected Void doInBackground(String... arg){
            final SharedPreferences.Editor prefsEdit = prefs.edit();

            if(mediaPlayer == null){
                initializeViews();
            }

            mediaPlayer.setLooping(false);
            mediaPlayer.start();

            int millisecond = mediaPlayer.getDuration();
            prefsEdit.putBoolean("mediaplaying",true);
            prefsEdit.commit();
            return null;
        }
    }

}
