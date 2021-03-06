package com.enhueco.view;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.enhueco.R;
import com.enhueco.model.logicManagers.FriendsManager;
import com.enhueco.model.model.EnHueco;
import com.enhueco.model.model.Event;
import com.enhueco.model.model.User;
import com.enhueco.model.other.BasicCompletionListener;
import com.enhueco.model.other.EHURLS;
import com.enhueco.model.other.Utilities;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class FriendDetailActivity extends AppCompatActivity
{
    private User friend;

    @Bind(R.id.imageImageView)
    ImageView imageImageView;
    @Bind(R.id.backgroundImageImageView)
    ImageView backgroundImageView;

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
        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(friend.getFirstNames());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        TextView firstNamesTextView = (TextView) findViewById(R.id.firstNamesTextView);
        firstNamesTextView.setText(friend.getFirstNames());

        TextView lastNamesTextView = (TextView) findViewById(R.id.lastNamesTextView);
        lastNamesTextView.setText(friend.getLastNames());

        for(int i = 1 ; i < friend.getSchedule().getWeekDays().length; i++)
        {
            for(Event event : friend.getSchedule().getWeekDays()[i].getEvents())
            {
                Log.v("FRIEND DETAIL EVENTS", event.getName() + " - " + event.getStartHour() + " - " +event
                        .getEndHour() + " - " + event.getStartHourWeekday() + " - " + event.getEndHourWeekday());
            }
        }
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getSupportActionBar().setTitle("En Hueco");
        getMenuInflater().inflate(R.menu.menu_friend_detail, menu);

        return true;
    }

    public void updateProfileImage()
    {
        if (friend.getImageURL().isPresent() && !friend.getImageURL().get().isEmpty())
        {
            Picasso.with(this).load(EHURLS.BASE + friend.getImageURL().get()).into(imageImageView, new Callback()
            {
                @Override
                public void onSuccess()
                {
                    new Handler(Looper.getMainLooper()).post(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            backgroundImageView.setImageBitmap(Utilities.fastblur(((BitmapDrawable) imageImageView.getDrawable()).getBitmap(), 0.1f, 20));
                        }
                    });
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
        Intent intent = new Intent(this, ScheduleActivity.class);
        intent.putExtra("userID", friend.getID());
        startActivity(intent);
    }

    public void onCallButtonPressed(MenuItem item)
    {
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:" + friend.getPhoneNumber()));

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED)
        {
            startActivity(callIntent);
        }
    }

    public void onWhatsAppButtonPressed(MenuItem item)
    {
        if (friend.getPhoneNumber() == null) return;

        Uri uri = Uri.parse("smsto:" + friend.getPhoneNumber());
        Intent i = new Intent(Intent.ACTION_SENDTO, uri);
        i.setPackage("com.whatsapp");
        startActivity(Intent.createChooser(i, ""));
    }

    public void onOptionsButtonPressed(MenuItem item)
    {
        AlertDialog.Builder addFriendMethodDialog = new AlertDialog.Builder(this);

        List<DialogOption> data = new ArrayList<>();
        data.add(new DialogOption("Eliminar amigo", null));
        ListAdapter la = new DialogOption.DialogOptionArrayAdapter(this, 0, data);

        addFriendMethodDialog.setSingleChoiceItems(la, -1, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(final DialogInterface dialog, int which)
            {
                FriendsManager.getSharedManager().deleteFriend(friend, new BasicCompletionListener()
                {
                    @Override
                    public void onSuccess()
                    {
                        dialog.dismiss();
                    }

                    @Override
                    public void onFailure(Exception error)
                    {

                    }
                });
            }
        });

        addFriendMethodDialog.show();
    }
}