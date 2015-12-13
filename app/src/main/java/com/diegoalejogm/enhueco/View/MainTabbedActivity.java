package com.diegoalejogm.enhueco.View;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.Toast;
import com.diegoalejogm.enhueco.Model.MainClasses.*;
import com.diegoalejogm.enhueco.Model.MainClasses.System;
import com.diegoalejogm.enhueco.R;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.ArrayList;
import java.util.List;

import static com.diegoalejogm.enhueco.View.CurrentlyAvailableFragment.*;

public class MainTabbedActivity extends AppCompatActivity implements FriendListFragment.OnFragmentInteractionListener, OnFragmentInteractionListener, TabLayout.OnTabSelectedListener
{

    private static final String LOG = "MainTabbedActivity";
    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private MainPagerAdapter mainPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager viewPager;
    private ArrayList<Integer> hiddenMenuItems;
    private TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_tabbed);
        hiddenMenuItems = new ArrayList<>();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("En Hueco");

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mainPagerAdapter = new MainPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        viewPager = (ViewPager) findViewById(R.id.container);
        viewPager.setAdapter(mainPagerAdapter);

        tabLayout = (TabLayout) findViewById(R.id.tabs);

        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setOnTabSelectedListener(this);

        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main_tabbed, menu);
        if (tabLayout.getSelectedTabPosition() == 0)
        {
            menu.findItem(R.id.action_add_friend).setVisible(false);
            menu.findItem(R.id.action_schedule).setVisible(false);
            menu.findItem(R.id.action_requests).setVisible(false);
            menu.findItem(R.id.action_log_out).setVisible(false);
            menu.findItem(R.id.action_qr_code).setVisible(false);
        }

        if (tabLayout.getSelectedTabPosition() == 1)
        {
            menu.findItem(R.id.action_log_out).setVisible(false);
            menu.findItem(R.id.action_qr_code).setVisible(false);
            menu.findItem(R.id.action_schedule).setVisible(false);
        }
        if (tabLayout.getSelectedTabPosition() == 2)
        {
            menu.findItem(R.id.action_search).setVisible(false);
            menu.findItem(R.id.action_requests).setVisible(false);
            menu.findItem(R.id.action_add_friend).setVisible(false);
        }

        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings)
//        {
//            return true;
//        }

        return super.onOptionsItemSelected(item);
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

        System.getInstance().logout(getApplicationContext());
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    public void showSchedule(MenuItem item)
    {
        Intent intent = new Intent(this, ScheduleActivity.class);
        intent.putExtra(ScheduleActivity.SCHEDULE_EXTRA, System.getInstance().getAppUser().getSchedule());
        startActivity(intent);
    }

    public void showQRCode(MenuItem item)
    {
        showQRCode();
    }

    public void showQRCode()
    {
        startActivity(new Intent(MainTabbedActivity.this, ShowQRActivity.class));
    }

    @Override
    protected void onResume()
    {
        super.onResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result.getContents() == null) {
            Log.d("MainActivity", "Cancelled scan");
            Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
        } else {
            Log.d("MainActivity", "Scanned");
            try
            {
                User friend = System.getInstance().getAppUser().addFriendFromStringEncodedFriendRepresentation(result.getContents());
                FriendListFragment fr = (FriendListFragment) mainPagerAdapter.getItem(1);
                fr.refresh();
                Toast.makeText(this, "El usuario " + friend.getUsername() + " ha sido agregado.", Toast.LENGTH_LONG).show();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    public void addFriend(MenuItem item)
    {
        AlertDialog.Builder addFriendMethodDialog = new AlertDialog.Builder(
                this);
        LayoutInflater factory = LayoutInflater.from(this);
        final View view = factory.inflate(R.layout.item_currently_available, null);

        List<DialogOption> data = new ArrayList<DialogOption>();
        data.add(new DialogOption("Buscar amigo", null));
        data.add(new DialogOption("Escanear código de amigo", null ));
        data.add(new DialogOption("Mostrar mi código", null ));
        ListAdapter la = new DialogOption.DialogOptionArrayAdapter(this, 0, data);

        addFriendMethodDialog.setSingleChoiceItems(la, -1, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                switch (which)
                {
                    case 0:
                        MainTabbedActivity.this.searchFriends();
                        break;
                    case 1:
                        MainTabbedActivity.this.scanQR();
                        break;
                    case 2:
                        MainTabbedActivity.this.showQRCode();
                        break;
                }
                dialog.dismiss();
            }
        });

        addFriendMethodDialog.show();
    }

    private void searchFriends()
    {
        Intent intent = new Intent(this, SearchNewFriendsActivity.class);
        startActivity(intent);
    }

    private void scanQR()
    {
        new IntentIntegrator(this).setCaptureActivity(CaptureQRActivityAnyOrientation.class).setBeepEnabled(true).setOrientationLocked(false).initiateScan();
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

    public void showRequests(MenuItem item)
    {
        Intent intent = new Intent(this, FriendRequestsActivity.class);
        startActivity(intent);
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
                    return new MyProfileFragment();
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
        public CharSequence getPageTitle(int position) { return tabNames[position]; }
    }
}
