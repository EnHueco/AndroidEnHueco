package com.diegoalejogm.enhueco.View;


import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
    }


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
            System.instance.createTestAppUser();
            finish();
        }
    }






}
