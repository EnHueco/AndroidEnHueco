package com.enhueco.view;


import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.*;
import android.widget.*;
import com.enhueco.model.logicManagers.FriendsManager;
import com.enhueco.model.model.*;
import com.enhueco.model.model.EnHueco;
import com.enhueco.R;
import com.enhueco.model.other.BasicCompletionListener;
import com.enhueco.model.other.CompletionListener;
import com.enhueco.model.other.EHURLS;
import com.enhueco.model.other.Utilities;
import com.enhueco.view.dialog.EHProgressDialog;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;
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

    List<UserSearch> filteredFriends;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_new_friends);

        ListView friendLV = (ListView) findViewById(R.id.searchFriendListView);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        filteredFriends = new ArrayList<>();
        adapter = new SearchFriendArrayAdapter(this, 0, filteredFriends);
        friendLV.setAdapter(adapter);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Agregar amigos");
    }

    private void updateResults(List<UserSearch> result)
    {
        filteredFriends.clear();
        filteredFriends.addAll(result);
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
                        final EHProgressDialog ehProgressDialog = new EHProgressDialog(SearchNewFriendsActivity.this);
                        ehProgressDialog.setMessage("Buscando");

                        if (!newText.isEmpty())
                        {
                            ehProgressDialog.show();
                            FriendsManager.getSharedManager().searchUsers(newText, new
                                    CompletionListener<List<UserSearch>>()
                                    {
                                        @Override
                                        public void onSuccess(List<UserSearch> result)
                                        {
                                            updateResults(result);
                                            ehProgressDialog.dismiss();
                                        }

                                        @Override
                                        public void onFailure(Exception error)
                                        {
                                            Utilities.showErrorToast(getApplicationContext());
                                            ehProgressDialog.dismiss();
                                        }
                                    });
                        }
                    }
                });
            }
        }, 450);
        return false;
    }

    public class SearchFriendArrayAdapter extends ArrayAdapter<UserSearch>
    {

        Context context;
        List<UserSearch> objects;

        public SearchFriendArrayAdapter(Context context, int resource, List<UserSearch> objects)
        {
            super(context, resource, objects);
            this.context = context;
            this.objects = objects;
        }

        @Override
        public boolean isEnabled(int position)
        {
            return false;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.item_friend_search, null);

            UserSearch user = objects.get(position);

            ImageView iv = (ImageView) view.findViewById(R.id.profileImageView);
            Transformation transformation = Utilities.getRoundTransformation();
            Picasso.with(context).load(EHURLS.BASE + user.getImageURL().get()).fit().transform(transformation).into(iv);

            TextView tv1 = (TextView) view.findViewById(R.id.fullNameTextView);
            final TextView tv2 = (TextView) view.findViewById(R.id.usernameTextView);
            tv1.setText(user.getFirstNames() + " " + user.getLastNames());
            tv2.setText(objects.get(position).getUsername());

            final FancyButton addFriendButton = (FancyButton) view.findViewById(R.id.btn_addFriend);
            // Hide "add button" if already friends
            if (EnHueco.getInstance().getAppUser().getFriends().containsKey(user.getUsername()))
            {
                addFriendButton.setVisibility(View.GONE);
            }
            else
            {
                addFriendButton.setVisibility(View.VISIBLE);
                addFriendButton.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        FriendsManager.getSharedManager().sendFriendRequestToUserRequestWithUsername(tv2.getText().toString(), new BasicCompletionListener()
                        {
                            @Override
                            public void onSuccess()
                            {
                                Toast.makeText(getApplicationContext(), "Solicitud enviada exitósamente", Toast
                                        .LENGTH_SHORT).show();
                            }

                            @Override
                            public void onFailure(Exception error)
                            {
                                Utilities.showErrorToast(getApplicationContext());
                            }
                        });

                        addFriendButton.setVisibility(View.INVISIBLE);
                    }
                });
            }

            return view;
        }
    }
}
