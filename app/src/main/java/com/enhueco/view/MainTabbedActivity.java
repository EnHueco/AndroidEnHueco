package com.enhueco.view;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.enhueco.R;
import com.enhueco.model.logicManagers.AccountManager;
import com.enhueco.model.logicManagers.ImmediateEventManager;
import com.enhueco.model.model.EnHueco;
import com.enhueco.model.model.immediateEvent.ImmediateEvent;
import com.enhueco.model.other.BasicCompletionListener;
import com.enhueco.model.other.Utilities;
import com.enhueco.view.dialog.EHProgressDialog;

import java.util.ArrayList;

public class MainTabbedActivity extends AppCompatActivity implements FriendListFragment.OnFragmentInteractionListener, CurrentlyAvailableFragment.OnFragmentInteractionListener, TabLayout.OnTabSelectedListener
{
    private static final String LOG = "MainTabbedActivity";

    public static final int APP_BAR_LAYOUT_SIZE = 90;

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private MainPagerAdapter mainPagerAdapter;

    @Bind(R.id.appbar)
    AppBarLayout appBarLayout;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    @Bind(R.id.container)
    ViewPager viewPager;

    private Menu optionsMenu;

    private ArrayList<Integer> hiddenMenuItems;
    @Bind(R.id.tabs)
    TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_tabbed);
        ButterKnife.bind(this);

        hiddenMenuItems = new ArrayList<>();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mainPagerAdapter = new MainPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        viewPager.setAdapter(mainPagerAdapter);

        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setOnTabSelectedListener(this);

        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getSupportActionBar().setTitle("En Hueco");

        optionsMenu = menu;

        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        switch (item.getItemId())
        {
            case R.id.menu_item_friend_requests:
                openFriendRequestsView();
        }

        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings)
//        {
//            return true;
//        }

        return super.onOptionsItemSelected(item);
    }


    public void openFriendRequestsView()
    {
        Intent intent = new Intent(this, FriendRequestsActivity.class);
        startActivity(intent);
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab)
    {
        invalidateOptionsMenu();
        viewPager.setCurrentItem(tab.getPosition());
        Log.v(LOG, "New position: " + tab.getPosition());
    }

    public void logOut(MenuItem item)
    {

        AccountManager.getSharedManager().logout();
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void addFriend(MenuItem item)
    {
        Intent intent = new Intent(this, SearchNewFriendsActivity.class);
        startActivity(intent);
    }

    @Override
    public void onFragmentInteraction(String id)
    {
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab)
    {
    }

    @Override
    public void onTabReselected(TabLayout.Tab tab)
    {
    }


    public AppBarLayout getAppBarLayout()
    {
        return appBarLayout;
    }

    public TabLayout getTabLayout()
    {
        return tabLayout;
    }

    public ViewPager getViewPager()
    {
        return viewPager;
    }

    public void onProfileViewScheduleButtonPressed(View view)
    {
        Intent intent = new Intent(this, ScheduleActivity.class);
        intent.putExtra(ScheduleActivity.SCHEDULE_EXTRA, EnHueco.getInstance().getAppUser().getSchedule());
        startActivity(intent);
    }

    public void onSettingsButtonPressed(MenuItem item)
    {
        startActivity(new Intent(this, SettingsActivity.class));
    }

    public void onTurnInvisibleButtonPressed(MenuItem item)
    {
        ImmediateEvent event = EnHueco.getInstance().getAppUser().getInstantFreeTimePeriod().get();
        if (event.isCurrentlyHappening() && event.getType().equals(ImmediateEvent.ImmediateEventType.INVISIBILITY))
        {
            turnVisible();
        }
        else
        {
            turnInvisible();
        }
    }

    private void turnInvisible()
    {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        int selectedOption = -1;
        CharSequence[] items = {"1:20 horas", "3 horas", "Resto del día"};
        final int[] selectedItemTime = {90, 180, Utilities.getSecondsUntilTomorrow()};


        alertDialog.setTitle("Duración").setSingleChoiceItems(items, 0, null).setPositiveButton("Aceptar", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                int position = ((AlertDialog)dialog).getListView().getCheckedItemPosition();
                        _turnInvisibleForInterval(selectedItemTime[position]);
                dialog.dismiss();
            }
        }).setNegativeButton("Cancelar", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.dismiss();
            }
        });
        alertDialog.show();
    }

    private void _turnInvisibleForInterval(int seconds)
    {
        final EHProgressDialog dialog = new EHProgressDialog(this);
        dialog.show();
        ImmediateEventManager.getSharedManager().turnInvisibleForTimeInterval(seconds, new BasicCompletionListener()
        {
            @Override
            public void onSuccess()
            {
                /*
                Drawable drawable = optionsMenu.findItem(R.id.action_turn_invisible).getIcon();

                if (drawable != null)
                {
                    drawable.mutate();
                    drawable.setColorFilter(Color.rgb(220, 170, 255), PorterDuff.Mode.SRC_ATOP);
                    drawable.setAlpha(1);
                }
                */
                dialog.dismiss();
            }

            @Override
            public void onFailure(Exception error)
            {
                dialog.dismiss();
                Utilities.showErrorToast(getApplicationContext());
            }
        });
    }

    private void turnVisible()
    {
        final EHProgressDialog dialog = new EHProgressDialog(this);
        dialog.show();
        ImmediateEventManager.getSharedManager().turnVisible(new BasicCompletionListener()
        {
            @Override
            public void onSuccess()
            {
                /*
                Drawable drawable = optionsMenu.findItem(R.id.action_turn_invisible).getIcon();

                if (drawable != null)
                {
                    drawable.mutate();
                    drawable.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
                    drawable.setAlpha(1);
                }
                */
                dialog.dismiss();
            }

            @Override
            public void onFailure(Exception error)
            {
                dialog.dismiss();
                Utilities.showErrorToast(MainTabbedActivity.this);
            }
        });
    }

    // TODO: Change profile image
    public void profileImagePressed(View view)
    {

    }

    public void onImAvailableButtonPressed(MenuItem item)
    {
        ImmediateEvent event = EnHueco.getInstance().getAppUser().getInstantFreeTimePeriod().get();

        if(EnHueco.getInstance().getAppUser().getCurrentFreeTimePeriod().isPresent())
        {
            Toast.makeText(this,"Ya te encuentras en hueco",Toast.LENGTH_SHORT).show();
        }

        else if(event.isCurrentlyHappening() && event.getType().equals(ImmediateEvent.ImmediateEventType.EVENT))
        {
            final EHProgressDialog dialog = new EHProgressDialog(this);
            dialog.show();
            ImmediateEventManager.getSharedManager().deleteInstantFreeTimeEvent(new BasicCompletionListener()
            {
                @Override
                public void onSuccess()
                {
                    dialog.dismiss();
                }

                @Override
                public void onFailure(Exception error)
                {
                    dialog.dismiss();
                    Utilities.showErrorToast(MainTabbedActivity.this);
                }
            });
        }
        else
        {
            InstantFreeTimeFragment fragment = InstantFreeTimeFragment.newInstance();
            fragment.show(getSupportFragmentManager(), "¡Estoy en Hueco!");
        }
    }

    public void onProfileImagePressed(View view)
    {
        pickImage();
    }

    private static final int PICK_PHOTO_FOR_AVATAR = 0;

    public void pickImage()
    {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_PHOTO_FOR_AVATAR);
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class MainPagerAdapter extends FragmentPagerAdapter
    {

        final String[] tabNames = {"En Hueco", "Amigos", "Mi perfil"};

        public MainPagerAdapter(FragmentManager fm)
        {
            super(fm);
        }

        @Override
        public Fragment getItem(int position)
        {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).

            switch (position)
            {
                case 0:
                    return new CurrentlyAvailableFragment();
                case 1:
                    return new FriendListFragment();
                case 2:
                    return new ProfileFragment();
            }
            return null;

        }

        @Override
        public int getCount()
        {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position)
        {
            return tabNames[position];
        }
    }
}
