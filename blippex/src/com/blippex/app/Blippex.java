package com.blippex.app;

import android.app.Application;
import android.content.Context;


public class Blippex extends Application {
    private static Context context;

    public void onCreate(){
        super.onCreate();
        Blippex.context = getApplicationContext();
    }

    public static Context getAppContext() {
        return Blippex.context;
    }
}

