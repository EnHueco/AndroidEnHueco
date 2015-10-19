package com.diegoalejogm.enhueco.View;


import android.content.*;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import com.diegoalejogm.enhueco.Model.MainClasses.*;
import com.diegoalejogm.enhueco.Model.MainClasses.System;
import com.diegoalejogm.enhueco.R;

public class LoginActivity extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        IntentFilter filterLogin = new IntentFilter(System.EHSystemNotification.SYSTEM_DID_LOGIN);
        IntentFilter filterLoginError = new IntentFilter(System.EHSystemNotification.SYSTEM_COULD_NOT_LOGIN_WITH_ERROR);
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, filterLogin
                );
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, filterLoginError
        );
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            if(intent.getAction().equals(System.EHSystemNotification.SYSTEM_DID_LOGIN))
            {
                intent = new Intent(LoginActivity.this, MainTabbedActivity.class);
                startActivity(intent);
                System.instance.persistData(getApplicationContext());
                LoginActivity.this.finish();
            }

//            Log.d("receiver", "Got message: " + message);
        }
    };


    public void logIn(View view)
    {
        EditText loginET = (EditText) findViewById(R.id.loginText);
        String loginString = loginET.getText().toString();
        EditText passwordET = (EditText) findViewById(R.id.passwordText);
        String passwordString= passwordET.getText().toString();

        if(loginString.equals("test") && passwordString.equals("test"))
        {
            Intent intent = new Intent(this, MainTabbedActivity.class);
            startActivity(intent);
            System.instance.createTestAppUser(getApplicationContext());
            finish();
        }
        else
        {
            System.instance.login(loginString, passwordString);
        }

    }






}
