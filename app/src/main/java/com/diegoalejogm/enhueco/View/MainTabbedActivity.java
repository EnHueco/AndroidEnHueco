package com.diegoalejogm.enhueco.View;

import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import com.diegoalejogm.enhueco.R;

import java.util.ArrayList;

public class MainTabbedActivity extends AppCompatActivity implements FriendsFragment.OnFragmentInteractionListener, InGapFragment.OnFragmentInteractionListener, TabLayout.OnTabSelectedListener
{

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
    TabLayout tabLayout;

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
            menu.findItem(R.id.action_log_out).setVisible(false);
            menu.findItem(R.id.action_qr_code).setVisible(false);
        }

        if (tabLayout.getSelectedTabPosition() == 1)
        {
            menu.findItem(R.id.action_log_out).setVisible(false);
            menu.findItem(R.id.action_qr_code).setVisible(false);
        }
        if (tabLayout.getSelectedTabPosition() == 2)
        {
            menu.findItem(R.id.action_search).setVisible(false);
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


    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class MainPagerAdapter extends FragmentPagerAdapter
    {

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
                    return new InGapFragment();
                case 1:
                    return new FriendsFragment();
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
        public CharSequence getPageTitle(int position)
        {
            switch (position)
            {
                case 0:
                    return "En Hueco";
                case 1:
                    return "Amigos";
                case 2:
                    return "Mi Perfil";
            }
            return null;
        }
    }
}
