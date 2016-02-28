package com.diegoalejogm.enhueco.view;


import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.*;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.diegoalejogm.enhueco.model.main.*;
import com.diegoalejogm.enhueco.model.main.System;
import com.diegoalejogm.enhueco.R;
import com.diegoalejogm.enhueco.model.other.BasicCompletionListener;
import com.diegoalejogm.enhueco.model.other.CompletionListener;
import mehdi.sakout.fancybuttons.FancyButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class SearchNewFriendsActivity extends AppCompatActivity implements MenuItemCompat.OnActionExpandListener, SearchView.OnQueryTextListener
{
    private static final String LOG = "SearchNewFriendsActivity";
    public static final String EXTRA_USERS = "EXTRA_USERS";
    private Timer mTimer;
    private SearchFriendArrayAdapter adapter;

    List<User> filteredFriends = new ArrayList<>(System.getInstance().getAppUser().getFriends().values());

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_new_friends);

        ListView friendLV = (ListView) findViewById(R.id.searchFriendListView);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        adapter = new SearchFriendArrayAdapter(this, 0, filteredFriends);
        friendLV.setAdapter(adapter);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Agregar amigos");
    }

    private void updateResults()
    {
        adapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_search_new_friends, menu);
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =
                (SearchView) menu.findItem(R.id.action_search_new).getActionView();

        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));

        SearchView.OnQueryTextListener queryTextListener = this;
        searchView.setOnQueryTextListener(queryTextListener);

        MenuItem searchMenuItem = menu.findItem(R.id.action_search_new);
        MenuItemCompat.expandActionView(searchMenuItem);
        MenuItemCompat.setOnActionExpandListener(searchMenuItem, this);

        return true;
    }

    @Override
    public boolean onMenuItemActionExpand(MenuItem item)
    {
        return false;
    }

    @Override
    public boolean onMenuItemActionCollapse(MenuItem item)
    {
        finish();
        return false;
    }

    @Override
    public boolean onQueryTextSubmit(String query)
    {
        return false;
    }

    @Override
    public boolean onQueryTextChange(final String newText)
    {
        if (mTimer != null)
        {
            mTimer.cancel();
        }

        mTimer = new Timer();
        mTimer.schedule(new TimerTask()
        {
            @Override
            public void run()
            {
                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        if(!newText.isEmpty()) System.getInstance().searchUsers(newText, new CompletionListener<List<User>>()
                        {
                            @Override
                            public void onSuccess(List<User> friends)
                            {
                                filteredFriends = friends;
                                updateResults();
                            }

                            @Override
                            public void onFailure(Exception error)
                            {
                                //TODO: Show error
                            }
                        });
                    }
                });
            }
        }, 500);
        return false;
    }

    public class SearchFriendArrayAdapter extends ArrayAdapter<User>
    {

        Context context;
        List<User> objects;

        public SearchFriendArrayAdapter(Context context, int resource, List<User> objects)
        {
            super(context, resource, objects);
            this.context = context;
            this.objects = objects;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.item_friend_search, null);

            TextView tv1 = (TextView) view.findViewById(R.id.fullNameTextView);
            final TextView tv2 = (TextView) view.findViewById(R.id.usernameTextView);
            tv1.setText(objects.get(position).toString());
            tv2.setText(objects.get(position).getUsername());

            final FancyButton addFriendButton = (FancyButton) view.findViewById(R.id.btn_addFriend);
            addFriendButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {

                    System.getInstance().getAppUser().sendFriendRequestToUserRequestWithUsername(tv2.getText().toString(), new BasicCompletionListener()
                    {
                        @Override
                        public void onSuccess()
                        {
                            //TODO
                        }

                        @Override
                        public void onFailure(Exception error)
                        {
                            //TODO
                        }
                    });

                    addFriendButton.setVisibility(View.INVISIBLE);
                }
            });

            return view;
        }
    }
}
