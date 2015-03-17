package com.example.raysmets.beatstream;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Looper;
import android.os.Message;

import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.content.SharedPreferences;
import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import org.apache.commons.io.IOUtils;

import org.apache.commons.io.IOUtils;

/**
 * Created by raysmets on 2/24/15.
 */
public class MusicPlayer extends ActionBarActivity implements MediaPlayer.OnCompletionListener {

    static MediaPlayer mediaPlayer;
    public static TextView songName, duration;
    public ImageView AlbumCoverImage;
    private static double timeElapsed = 0, finalTime = 0;
    private static int forwardTime = 2000, backwardTime = 2000;
    private static Handler durationHandler = new Handler();
    private static SeekBar seekbar;
    SharedPreferences prefs;
    static boolean Playing;
    private static final String TAG = "Musicplayer";
    private String FileName;
    private static int songID;
    private static String songTitle;
    private String songArtist;
    private int songDurationMS;
    private String songDuration;
    //private Bitmap albumCover;
    private BlockingQueue<byte[]> bytesQ;
    private JoinService joinService;
    public playBytesThread playThread;
    AudioTrack track;
    int BUFFER_SIZE;// = 4000;
    Context context;
    MediaStream mediaStream;

    public MusicPlayer(BlockingQueue<byte[]> bytes) {
        bytesQ = bytes;
        playThread = new playBytesThread(bytesQ, this);
        mediaPlayer = new MediaPlayer();
        //playThread.start();


        //audiotrack
        BUFFER_SIZE = AudioTrack.getMinBufferSize(44100,AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT);
        Log.i("MINIMUM_BUFFER_SIZE", String.valueOf(BUFFER_SIZE));
        track = new AudioTrack(AudioManager.STREAM_MUSIC, 44100,
                AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT,
                BUFFER_SIZE, AudioTrack.MODE_STREAM);
        track.play();
    }
    boolean first = true;
    public void add(byte[] bytes){
        track.write(bytes, 0, bytes.length);
        //if(bytesQ == null)
         //   Log.d(TAG, "bytesQ is NULLLLLLLLLLLLL!!!!!");
        //playThread.add(bytes);


    }

    public MusicPlayer(){

    }

    private static Bitmap albumCover;

    byte[] albumbytes;
    int [] songs;
    int current_index = songID;
    MediaMetadataRetriever metaData = new MediaMetadataRetriever();


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
        albumbytes = intent.getByteArrayExtra("albumCover");
        joinService = MyApplication.getJoinService();
        mediaStream = new MediaStream(joinService);

        songs = new int[] {R.raw.all_of_me,R.raw.apologize,R.raw.sample_song,R.raw.strongerkw};
        joinService = MyApplication.getJoinService();
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
            mediaPlayer.setOnCompletionListener(this);
        }
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer){
        play();
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
               mediaPlayer = MediaPlayer.create(this, songs[current_index]);
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

    }

    private void sendMusic(int sample_song) throws IOException {
        Context contx = MyApplication.getAppContext();
        if(contx == null)
            Log.d(TAG, "context is null@#$@#$#@$%@%^#$%^&$%&%^&$^");
        byte[] payload = IOUtils.toByteArray(contx.getResources().openRawResource(sample_song));
        Log.i(TAG, "Number of bits in song: " + String.valueOf(payload.length));
        Log.i(TAG, "sending the byte array to joinService");
        joinService.write(payload);
    }

    public void playbytes(byte[] bytes){
        track.write(bytes, 0, bytes.length);
        try {
            Log.d(TAG, "in playbytes method!!!!!");
            // create temp file that will hold byte array
            File tempMp3 = File.createTempFile("kurchina", "mp3", MyApplication.getAppContext().getCacheDir());
//            tempMp3.deleteOnExit();
//            FileOutputStream fos = new FileOutputStream(tempMp3);
//            fos.write(bytes);
//            fos.close();
//



//            // Tried reusing instance of media player
//            // but that resulted in system crashes...
//
//            mediaPlayer.reset();
//
//            // Tried passing path directly, but kept getting
//            // "Prepare failed.: status=0x1"
//            // so using file descriptor instead
//            FileInputStream fis = new FileInputStream(tempMp3);
//            Log.d(TAG, "here.......2");
//            mediaPlayer.setDataSource(fis.getFD());
//            Log.i(TAG, "playing recieved bytes");
//            mediaPlayer.prepare();
//            mediaPlayer.start();
        } catch (IOException ex) {
            String s = ex.toString();
            ex.printStackTrace();
        }
    }

    // play mp3 song
    public void play(View view) {
        Playing = true;
        if(!mediaPlayer.isPlaying()) {
            playMusic();
        }
        durationHandler.postDelayed(updateSeekBarTime, 100);
        try {
            Log.i(TAG, "beginning the process of sending audio");
            //sendMusic(songID);
            mediaStream.sendMusic(songID);
            Log.i(TAG, "DONE SENDING AUDiO");
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    private class playBytesThread extends Thread{
        public BlockingQueue<byte[]> playQ;
        public MusicPlayer musicPlayer;

        public playBytesThread(BlockingQueue<byte[]> bytes, MusicPlayer mp){
            musicPlayer = mp;
            playQ = bytes;
        }

        public void add(byte[] b){
            playQ.add(b);
        }

        public void run(){
            Log.i(TAG, "starting playBytesThread");

            byte[] bytess; //= new byte[1024];
            while(true){
                try {
                    Log.d(TAG, "trying to grab bytes in playBytesThread");
                    bytess = playQ.take();
                    if(bytess != null) Log.d(TAG,"successfully grabbed bytes in playBytesThread");
                    musicPlayer.playbytes(bytess);
                } catch (InterruptedException e) {
                    Log.d(TAG, "can't grab audio bytes");
                    e.printStackTrace();
                }

            }
        }
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

    private void play(){
        current_index = (current_index+1)%4;
        AssetFileDescriptor afd = this.getResources().openRawResourceFd(songs[current_index]);

        metaData.setDataSource(afd.getFileDescriptor(),afd.getStartOffset(),afd.getDeclaredLength());
        songTitle = metaData.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
        songName = (TextView) findViewById(R.id.songName);
        songName.setText(songTitle);
        //AlbumCoverImage = (ImageView) findViewById(R.id.mp3Image);

        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(afd.getFileDescriptor(),afd.getStartOffset(),afd.getDeclaredLength());
            mediaPlayer.prepare();
            mediaPlayer.start();
            finalTime = mediaPlayer.getDuration();
            duration = (TextView) findViewById(R.id.songDuration);
            durationHandler.postDelayed(updateSeekBarTime, 100);
            afd.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


}
