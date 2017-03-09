package com.dawnjf.fei.perfectweather.util;

import android.app.Application;
import android.content.Context;

import org.litepal.LitePal;

/**
 * Created by fei on 2017/3/3.
 */

public class MyApplication extends Application {

    private static Context sContext;

    @Override
    public void onCreate() {
        super.onCreate();
        sContext = getApplicationContext();
        LitePal.initialize(this);
    }

    public static Context getContext() {
        return sContext;
    }
}
