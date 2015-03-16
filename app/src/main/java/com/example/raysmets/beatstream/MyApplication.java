package com.example.raysmets.beatstream;

import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.graphics.Paint;

/**
 * Created by raysmets on 3/15/15.
 */
public class MyApplication extends Application{

    private static Context context;
    private static JoinService joinService;

    public void onCreate(){
        super.onCreate();
        MyApplication.context = getApplicationContext();
        joinService = new JoinService(context);
    }

    public static Context getAppContext() {
        return MyApplication.context;
    }

    public static BluetoothAdapter getBTadapter(){
        return BluetoothAdapter.getDefaultAdapter();
    }

    public static JoinService getJoinService(){
        return joinService;
    }
}
