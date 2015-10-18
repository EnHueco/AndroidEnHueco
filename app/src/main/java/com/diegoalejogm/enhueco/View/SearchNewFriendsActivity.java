package com.diegoalejogm.enhueco.View;



import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.diegoalejogm.enhueco.Model.MainClasses.*;
import com.diegoalejogm.enhueco.Model.MainClasses.System;
import com.diegoalejogm.enhueco.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class SearchNewFriendsActivity extends AppCompatActivity implements MenuItemCompat.OnActionExpandListener, SearchView.OnQueryTextListener
{

    private static final String LOG = "SearchNewFriendsActivity";
    private Timer mTimer;
    private SearchFriendArrayAdapter adapter;
    ArrayList<User> users;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_new_friends);

        ListView friendLV = (ListView) findViewById(R.id.searchFriendListView);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        users = new ArrayList<User>();
        adapter = new SearchFriendArrayAdapter(this, 0, users);
        friendLV.setAdapter(adapter);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Agregar amigos");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
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
        if (mTimer != null) {
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
                        Log.v(LOG, newText);
                        users.addAll(System.instance.getAppUser().getFriends());
                        adapter.notifyDataSetChanged();
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
            View view = inflater.inflate(R.layout.item_friend_request, null);

            TextView tv1 = (TextView) view.findViewById(R.id.fullNameTextView);
            TextView tv2 = (TextView) view.findViewById(R.id.usernameTextView);
            tv1.setText(objects.get(position).toString());
            tv2.setText(objects.get(position).getUsername());

            return view;
        }
    }
}
