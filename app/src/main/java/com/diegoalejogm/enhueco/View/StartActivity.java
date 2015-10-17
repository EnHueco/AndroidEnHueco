package com.diegoalejogm.enhueco.View;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import com.diegoalejogm.enhueco.Model.MainClasses.*;
import com.diegoalejogm.enhueco.Model.MainClasses.System;

public class StartActivity extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        Intent intent;
        boolean loggedIn = System.instance.loadDataFromPersistence(getApplicationContext());

        if (loggedIn) intent = new Intent(this, MainTabbedActivity.class);
        else intent = new Intent(this, LoginActivity.class);
        startActivity(intent);

        finish();
    }
}
