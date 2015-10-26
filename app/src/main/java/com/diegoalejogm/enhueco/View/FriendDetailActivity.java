package com.diegoalejogm.enhueco.View;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.diegoalejogm.enhueco.Model.MainClasses.System;
import com.diegoalejogm.enhueco.Model.MainClasses.User;
import com.diegoalejogm.enhueco.R;
import com.squareup.picasso.Picasso;
import mehdi.sakout.fancybuttons.FancyButton;

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

        for (User user : System.instance.getAppUser().getFriends())
        {
            if (user.getID().equals(getIntent().getStringExtra("friendID")))
            {
                friend = user;
                break;
            }
        }

        setContentView(R.layout.activity_friend_detail);

        FancyButton whatsappFB = (FancyButton) findViewById(R.id.fancyBtnWhatsapp);
        FancyButton callFB = (FancyButton) findViewById(R.id.fancyBtnCall);

        if (friend.getPhoneNumber() == null || friend.getPhoneNumber().isEmpty())
        {
            whatsappFB.setVisibility(View.GONE);
            callFB.setVisibility(View.GONE);
        }

        else
        {
            callFB.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    Intent intent = new Intent(Intent.ACTION_CALL);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                    intent.setData(Uri.parse("tel:" + friend.getPhoneNumber()));
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                    {
                        if (checkSelfPermission(Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED)
                        {
                            // TODO: Consider calling
                            //    public void requestPermissions(@NonNull String[] permissions, int requestCode)
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for Activity#requestPermissions for more details.
                            return;
                        }
                    }
                    FriendDetailActivity.this.getApplicationContext().startActivity(intent);
                }
            });

            whatsappFB.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    Uri uri = Uri.parse("smsto:" + friend.getPhoneNumber());
                    Intent intent = new Intent(Intent.ACTION_SENDTO, uri);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setPackage("com.whatsapp");
//                    mIntent.putExtra("sms_body", "The text goes here");
                    intent.putExtra("chat",true);
                    FriendDetailActivity.this.getApplicationContext().startActivity(intent);
                }
            });
        }

        FancyButton crossGapsFB = (FancyButton) findViewById(R.id.fancyBtnCrossGaps);
        TextView friendNameTextView = (TextView) findViewById(R.id.friendNameTextView);
        TextView friendUsernameTextView = (TextView) findViewById(R.id.friendUsernameTextView);
        ImageView friendImageImageView = (ImageView) findViewById(R.id.friendImageImageView);

        whatsappFB.setFontIconSize(35);
        callFB.setFontIconSize(35);
        crossGapsFB.setFontIconSize(35);
        crossGapsFB.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                onCommonGapsButtonPressed(v);
            }
        });

        friendNameTextView.setText(friend.getName());
        friendUsernameTextView.setText(friend.getUsername());

        if (friend.getImageURL().isPresent() && !friend.getImageURL().get().isEmpty())
        {
            friendNameTextView.setTextColor(Color.parseColor("#FFFFFF"));
            friendUsernameTextView.setTextColor(Color.parseColor("#FFFFFF"));
            Picasso.with(this).load(friend.getImageURL().get()).into(friendImageImageView);
        }
        else
        {
            friendNameTextView.setTextColor(Color.parseColor("#000000"));
            friendUsernameTextView.setTextColor(Color.parseColor("#000000"));
        }
    }

    public void onCommonGapsButtonPressed (View view)
    {
        Intent intent = new Intent(this, CommonGapsActivity.class);
        intent.putExtra("initialFriendID", friend.getID());
        startActivity(intent);
    }
}
