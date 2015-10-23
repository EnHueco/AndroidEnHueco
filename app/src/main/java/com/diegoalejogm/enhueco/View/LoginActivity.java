package com.diegoalejogm.enhueco.View;


import android.app.ProgressDialog;
import android.content.*;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import com.diegoalejogm.enhueco.Model.MainClasses.*;
import com.diegoalejogm.enhueco.Model.MainClasses.System;
import com.diegoalejogm.enhueco.R;

public class LoginActivity extends AppCompatActivity
{

    ProgressDialog loginProgressDialog;
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

        loginProgressDialog = new ProgressDialog(this);
        loginProgressDialog.setMessage("Ingresando...");

    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, final Intent intent) {
            // Get extra data included in the Intent

                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    public void run() {
                        if ((loginProgressDialog != null) && loginProgressDialog.isShowing()) {
                            loginProgressDialog.dismiss();
                        }
                        if(intent.getAction().equals(System.EHSystemNotification.SYSTEM_DID_LOGIN))
                        {
                            Intent intent = new Intent(LoginActivity.this, MainTabbedActivity.class);
                            startActivity(intent);
                            System.instance.persistData(getApplicationContext());
                            LoginActivity.this.finish();
                        }
                        else if(intent.getAction().equals(System.EHSystemNotification.SYSTEM_COULD_NOT_LOGIN_WITH_ERROR))
                        {
                            Toast.makeText(getApplicationContext(), "Credenciales inválidas", Toast.LENGTH_SHORT).show();
                        }
                    }
                }, 400);
            }

//            Log.d("receiver", "Got message: " + message);
    };


    public void logIn(View view)
    {
        loginProgressDialog.show();
        EditText loginET = (EditText) findViewById(R.id.loginText);
        String loginString = loginET.getText().toString();
        EditText passwordET = (EditText) findViewById(R.id.passwordText);
        String passwordString= passwordET.getText().toString();

        // Testing values
        if(loginString.equals("test") && passwordString.equals("test"))
        {
            Intent intent = new Intent(this, MainTabbedActivity.class);
            startActivity(intent);
            System.instance.createTestAppUser(getApplicationContext());
            finish();
        }
        // Actual login
        else
        {
            System.instance.login(loginString, passwordString, getApplicationContext());
        }

    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        if (loginProgressDialog.isShowing()) {
            loginProgressDialog.cancel();
        }
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        if (loginProgressDialog.isShowing()) {
            loginProgressDialog.cancel();
        }
    }
}
