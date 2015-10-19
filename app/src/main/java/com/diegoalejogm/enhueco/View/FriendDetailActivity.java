package com.diegoalejogm.enhueco.View;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.diegoalejogm.enhueco.Model.EHApplication;
import com.diegoalejogm.enhueco.Model.MainClasses.*;
import com.diegoalejogm.enhueco.Model.MainClasses.System;
import com.diegoalejogm.enhueco.R;
import com.squareup.picasso.Picasso;

public class FriendDetailActivity extends Activity
{
    private User friend;

    public FriendDetailActivity()
    {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        for (User user: System.instance.getAppUser().getFriends())
        {
            if (user.getID().equals(getIntent().getStringExtra("friendID")))
            {
                friend = user;
            }
        }

        setContentView(R.layout.activity_friend_detail);

        Button commonGapsButton = (Button) findViewById(R.id.commonGapsButton);
        commonGapsButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                onCommonGapsButtonPressed(v);
            }
        });

        TextView friendNameTextView = (TextView) findViewById(R.id.friendNameTextView);
        friendNameTextView.setText(friend.getName());

        TextView friendUsernameTextView = (TextView) findViewById(R.id.friendUsernameTextView);
        friendUsernameTextView.setText(friend.getUsername());

        ImageView friendImageImageView = (ImageView) findViewById(R.id.friendImageImageView);

        if (friend.getImageURL().isPresent())
        {
            Picasso.with(this).load(friend.getImageURL().get()).into(friendImageImageView);
        }
    }

    public void onCommonGapsButtonPressed (View view)
    {
        Intent intent = new Intent(this, CommonGapsActivity.class);
        intent.putExtra("initialFriendID", friend.getID());
        startActivity(intent);
    }
}
