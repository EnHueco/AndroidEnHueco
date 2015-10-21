package com.diegoalejogm.enhueco.View;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.diegoalejogm.enhueco.Model.MainClasses.*;
import com.diegoalejogm.enhueco.Model.MainClasses.System;
import com.diegoalejogm.enhueco.R;
import mehdi.sakout.fancybuttons.FancyButton;

import java.util.ArrayList;
import java.util.List;

public class FriendRequestsActivity extends AppCompatActivity
{
    public static final String EXTRA_REQUESTS = "EXTRA_REQUESTS" ;
    ListView friendRequestsLV;
    List<User> requests;
    FriendRequestArrayAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_requests);
        friendRequestsLV = (ListView) findViewById(R.id.requestsListView);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Solicitudes");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        IntentFilter filterSearch = new IntentFilter(System.EHSystemNotification.SYSTEM_DID_RECEIVE_FRIEND_REQUEST_UPDATES);
        IntentFilter filterAccepted = new IntentFilter(System.EHSystemNotification.SYSTEM_DID_RECEIVE_FRIEND_REQUEST_ACCEPT);
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, filterSearch);
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, filterAccepted);

        requests = new ArrayList<>();
        System.instance.getAppUser().fetchFriendRequests();
        adapter = new FriendRequestArrayAdapter(this, 0, requests);
        friendRequestsLV.setAdapter(adapter);

    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver()
    {

        @Override
        public void onReceive(Context context, Intent intent)
        {
            // Get extra data included in the Intent
            if (intent.getAction().equals(System.EHSystemNotification.SYSTEM_DID_RECEIVE_FRIEND_REQUEST_UPDATES))
            {
                ArrayList<User> users = (ArrayList<User>) intent.getSerializableExtra(FriendRequestsActivity.EXTRA_REQUESTS);
                FriendRequestsActivity.this.updateRequests(users);
            }
            else if(intent.getAction().equals(System.EHSystemNotification.SYSTEM_DID_RECEIVE_FRIEND_REQUEST_ACCEPT))
            {
                System.instance.getAppUser().fetchFriendRequests();
            }

//            Log.d("receiver", "Got message: " + message);
        }
    };

    private void updateRequests(ArrayList<User> users)
    {
        requests.clear();
        requests.addAll(users);
        adapter.notifyDataSetChanged();
    }

    public class FriendRequestArrayAdapter extends ArrayAdapter<User>
    {

        Context context;
        List<User> objects;

        public FriendRequestArrayAdapter(Context context, int resource, List<User> objects)
        {
            super(context, resource, objects);
            this.context = context;
            this.objects = objects;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            final User user = objects.get(position);

            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.item_friend_request, null);

            TextView tv1 = (TextView) view.findViewById(R.id.fullNameTextView);
            TextView tv2 = (TextView) view.findViewById(R.id.usernameTextView);

            FancyButton fb = (FancyButton) view.findViewById(R.id.btn_acceptFriendship);

            tv1.setText(user.toString());
            tv2.setText(user.getUsername());

            fb.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    System.instance.getAppUser().acceptFriendRequestToUserRequestWithUsername(user.getUsername());
                }
            });

            return view;
        }
    }
}
