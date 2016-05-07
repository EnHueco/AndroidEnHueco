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
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.enhueco.R;
import com.enhueco.model.logicManagers.FriendsManager;
import com.enhueco.model.model.EnHueco;
import com.enhueco.model.model.User;
import com.enhueco.model.other.BasicCompletionListener;
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
    List<User> requests;
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


        // Broadcast managers
        IntentFilter filterSearch = new IntentFilter(EnHueco.EHSystemNotification.SYSTEM_DID_RECEIVE_FRIEND_REQUEST_UPDATES);
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, filterSearch);

        // Adapter
        requests = new ArrayList<>();
        adapter = new FriendRequestArrayAdapter(this, 0, requests);
        friendRequestsLV.setAdapter(adapter);
        friendRequestsLV.setEmptyView(emptyRefreshLayout);

        fetchFriendRequests();

    }

    private void fetchFriendRequests()
    {
        FriendsManager.getSharedManager().fetchFriendRequests();
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver()
    {

        @Override
        public void onReceive(Context context, Intent intent)
        {
            // Get extra data included in the Intent
            if (intent.getAction().equals(EnHueco.EHSystemNotification.SYSTEM_DID_RECEIVE_FRIEND_REQUEST_UPDATES))
            {
                ArrayList<User> users = (ArrayList<User>) intent.getSerializableExtra(FriendRequestsActivity.EXTRA_REQUESTS);
                FriendRequestsActivity.this.updateRequests(users);
            }
        }
    };

    private void updateRequests(ArrayList<User> users)
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

            ImageView iv = (ImageView) view.findViewById(R.id.requestIcon);

            if (user.getImageURL().isPresent())
            {
                Picasso.with(context).load(EHURLS.BASE + user.getImageThumbnail()).fit().into(iv);
            }

            FancyButton fb = (FancyButton) view.findViewById(R.id.btn_acceptFriendship);

            tv1.setText(user.toString());
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
                            FriendsManager.getSharedManager().fetchFriendRequests();
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
