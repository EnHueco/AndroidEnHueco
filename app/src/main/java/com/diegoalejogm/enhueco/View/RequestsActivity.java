package com.diegoalejogm.enhueco.View;

import android.app.Activity;
import android.content.Context;
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

import java.util.List;

public class RequestsActivity extends AppCompatActivity
{
    ListView friendRequestsLV;

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

        List<User> requests = System.instance.getAppUser().getIncomingFriendRequests();
        requests.addAll(System.instance.getAppUser().getFriends());
        FriendRequestArrayAdapter adapter = new FriendRequestArrayAdapter(this, 0, requests);
        friendRequestsLV.setAdapter(adapter);
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
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.item_friend_request, null);

            TextView tv1 = (TextView) view.findViewById(R.id.fullNameTextView);
            TextView tv2 = (TextView) view.findViewById(R.id.usernameTextView);
            tv1.setText(objects.get(position).toString());
            tv2.setText(objects.get(position).getUsername());

            return view;
        }
    }
}
