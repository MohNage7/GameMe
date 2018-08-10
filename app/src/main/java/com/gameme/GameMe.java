package com.gameme;

import android.app.Application;

public class GameMe extends Application {
    private static GameMe instance;

    public static GameMe get() {
        return instance;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }
}
