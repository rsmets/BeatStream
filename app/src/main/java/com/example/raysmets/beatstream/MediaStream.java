package com.example.raysmets.beatstream;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

import org.apache.commons.io.IOUtils;

import java.io.IOException;

/**
 * Created by raysmets on 3/16/15.
 */
public class MediaStream {

    AudioTrack track;
    int BUFFER_SIZE;
    JoinService joinService;

    private static final String TAG = "MediaStream";

    public MediaStream(JoinService js){
        BUFFER_SIZE = AudioTrack.getMinBufferSize(44100, AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT);
        Log.i("MINIMUM_BUFFER_SIZE", String.valueOf(BUFFER_SIZE));
        track = new AudioTrack(AudioManager.STREAM_MUSIC, 44100,
                AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT,
                BUFFER_SIZE, AudioTrack.MODE_STREAM);
        track.play();

        joinService = js;
    }

    public void add(byte[] bytes){
        track.write(bytes, 0, bytes.length);

    }

    public void sendMusic(int sample_song) throws IOException {
        Context contx = MyApplication.getAppContext();

        byte[] payload = IOUtils.toByteArray(contx.getResources().openRawResource(sample_song));
        Log.i(TAG, "Number of bits in song: " + String.valueOf(payload.length));
        Log.i(TAG, "sending the byte array to joinService");
        joinService.write(payload);
    }
}
