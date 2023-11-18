package com.demo.amime;

import android.app.Application;

public class App extends Application {
    static {
        System.loadLibrary("opencv_java4");
    }
    @Override
    public void onCreate() {
        super.onCreate();
    }
}
