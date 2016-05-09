package com.enhueco.model;

import android.app.Application;
import android.content.Context;
import com.mikepenz.iconics.context.IconicsLayoutInflater;
import net.danlew.android.joda.JodaTimeAndroid;

/**
 * Created by Diego on 10/12/15.
 */
public class EHApplication extends Application
{
    private static Context context;

    @Override
    public void onCreate()
    {
        super.onCreate();
        JodaTimeAndroid.init(this);
        EHApplication.context = getApplicationContext();

    }

    public static Context getAppContext()
    {
        return EHApplication.context;
    }
}
