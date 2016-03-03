package com.enhueco.model;

import android.app.Application;
import android.content.Context;

/**
 * Created by Diego on 10/12/15.
 */
public class EHApplication extends Application
{
    private static Context context;

    public void onCreate()
    {
        super.onCreate();
        EHApplication.context = getApplicationContext();
    }

    public static Context getAppContext()
    {
        return EHApplication.context;
    }
}
