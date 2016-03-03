package com.enhueco.view;

import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.enhueco.model.model.EnHueco;
import com.enhueco.model.model.User;
import com.enhueco.model.other.EHURLS;
import com.enhueco.model.other.Utilities;
import com.enhueco.R;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

public class FriendDetailActivity extends AppCompatActivity
{
    private User friend;

    private ImageView imageImageView;
    private ImageView backgroundImageView;


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        for (User user : EnHueco.getInstance().getAppUser().getFriends().values())
        {
            if (user.getID().equals(getIntent().getStringExtra("friendID")))
            {
                friend = user;
                break;
            }
        }

        setContentView(R.layout.activity_friend_detail);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(friend.getID());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        TextView firstNamesTextView = (TextView) findViewById(R.id.firstNamesTextView);
        firstNamesTextView.setText(friend.getFirstNames());

        TextView lastNamesTextView = (TextView) findViewById(R.id.lastNamesTextView);
        lastNamesTextView.setText(friend.getLastNames());

        imageImageView = (ImageView) findViewById(R.id.imageImageView);
        backgroundImageView = (ImageView) findViewById(R.id.backgroundImageImageView);

        /*FancyButton whatsappFB = (FancyButton) findViewById(R.id.fancyBtnWhatsapp);
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

        FancyButton commonFreeTimePeriodsFB = (FancyButton) findViewById(R.id.fancyBtnCommonFreeTimePeriods);
        ImageView friendImageImageView = (ImageView) findViewById(R.id.friendImageImageView);

        whatsappFB.setFontIconSize(35);
        callFB.setFontIconSize(35);
        commonFreeTimePeriodsFB.setFontIconSize(35);
        commonFreeTimePeriodsFB.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                onCommonFreeTimePeriodsButtonPressed(v);
            }
        });*/
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        updateProfileImage();
    }

    public void updateProfileImage ()
    {
        if (friend.getImageURL().isPresent() && !friend.getImageURL().get().isEmpty())
        {
            Picasso.with(this).load(EHURLS.BASE + friend.getImageURL().get()).into(imageImageView);
            Picasso.with(this).load(EHURLS.BASE + friend.getImageURL().get()).into(backgroundImageView, new Callback()
            {
                @Override
                public void onSuccess()
                {
                    backgroundImageView.setImageBitmap(Utilities.fastblur(((BitmapDrawable) backgroundImageView.getDrawable()).getBitmap(), 1, 120));
                }

                @Override
                public void onError()
                {
                }
            });
        }
    }

    public void onCommonFreeTimePeriodsButtonPressed(View view)
    {
        Intent intent = new Intent(this, CommonFreeTimePeriodsActivity.class);
        intent.putExtra("initialFriendID", friend.getID());
        startActivity(intent);
    }

    public void onViewScheduleButtonPressed(View view)
    {
        Intent intent = new Intent(this, CommonFreeTimePeriodsActivity.class);
        intent.putExtra("initialFriendID", friend.getID());
        startActivity(intent);
    }
}
