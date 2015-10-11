package com.diegoalejogm.enhueco.View;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import com.diegoalejogm.enhueco.R;

public class AddFriendSelectActivity extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend_select);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Agregar Amigo");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }
}
