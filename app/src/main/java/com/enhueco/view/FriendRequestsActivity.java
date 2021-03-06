package com.enhueco.view;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.bumptech.glide.util.Util;
import com.enhueco.R;
import com.enhueco.model.logicManagers.FriendsManager;
import com.enhueco.model.model.EnHueco;
import com.enhueco.model.model.User;
import com.enhueco.model.model.UserSearch;
import com.enhueco.model.other.BasicCompletionListener;
import com.enhueco.model.other.CompletionListener;
import com.enhueco.model.other.EHURLS;
import com.enhueco.model.other.Utilities;
import com.squareup.picasso.Picasso;
import mehdi.sakout.fancybuttons.FancyButton;

import java.util.ArrayList;
import java.util.List;

public class FriendRequestsActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener
{
    public static final String EXTRA_REQUESTS = "EXTRA_REQUESTS" ;
    ListView friendRequestsLV;
    List<UserSearch> requests;
    FriendRequestArrayAdapter adapter;
    SwipeRefreshLayout friendRefreshLayout, emptyRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_requests);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        friendRequestsLV = (ListView) findViewById(R.id.requestsListView);
        friendRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshRequestList);
        emptyRefreshLayout= (SwipeRefreshLayout) findViewById(R.id.swipeRefreshEmpty);

        // Toolbar
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Solicitudes");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Refresh layouts
        emptyRefreshLayout.setOnRefreshListener(this);
        friendRefreshLayout.setOnRefreshListener(this);
        emptyRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                emptyRefreshLayout.setRefreshing(true);
            }
        });


        // Adapter
        requests = new ArrayList<>();
        adapter = new FriendRequestArrayAdapter(this, 0, requests);
        friendRequestsLV.setAdapter(adapter);
        friendRequestsLV.setEmptyView(emptyRefreshLayout);

        fetchFriendRequests();

    }

    private void fetchFriendRequests()
    {
        FriendsManager.getSharedManager().fetchFriendRequests(new CompletionListener<ArrayList<UserSearch>>()
        {
            @Override
            public void onSuccess(ArrayList<UserSearch> result)
            {
                updateRequests(result);
            }

            @Override
            public void onFailure(Exception error)
            {
                Utilities.showErrorToast(FriendRequestsActivity.this);
            }
        });
    }

    private void updateRequests(ArrayList<UserSearch> users)
    {
        friendRefreshLayout.setRefreshing(false);
        emptyRefreshLayout.setRefreshing(false);

        requests.clear();
        requests.addAll(users);

        adapter.notifyDataSetChanged();
    }

    @Override
    public void onRefresh()
    {
        fetchFriendRequests();
    }

    public class FriendRequestArrayAdapter extends ArrayAdapter<UserSearch>
    {

        Context context;
        List<UserSearch> objects;

        public FriendRequestArrayAdapter(Context context, int resource, List<UserSearch> objects)
        {
            super(context, resource, objects);
            this.context = context;
            this.objects = objects;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent)
        {
            final UserSearch user = objects.get(position);

            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.item_friend_request, null);

            TextView tv1 = (TextView) view.findViewById(R.id.fullNameTextView);
            TextView tv2 = (TextView) view.findViewById(R.id.usernameTextView);

            ImageView iv = (ImageView) view.findViewById(R.id.requestIcon);

            if (user.getImageURL().isPresent())
            {
                Picasso.with(context).load(EHURLS.BASE + user.getImageThumbnail()).fit().into(iv);
            }

            FancyButton fb = (FancyButton) view.findViewById(R.id.btn_acceptFriendship);

            tv1.setText(user.getFirstNames() + " " + user.getLastNames());
            tv2.setText(user.getUsername());

            fb.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    FriendsManager.getSharedManager().acceptFriendRequestFromUserWithUsername(user.getUsername(), new BasicCompletionListener()
                    {
                        @Override
                        public void onSuccess()
                        {
//                            FriendsManager.getSharedManager().fetchFriendRequests();
                            Toast.makeText(FriendRequestsActivity.this, "Solicitud aceptada", Toast
                                    .LENGTH_SHORT).show();
                            objects.remove(position);
                            adapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onFailure(Exception error)
                        {
                            Utilities.showErrorToast(getApplicationContext());
                        }
                    });
                }
            });

            return view;
        }
    }
}
