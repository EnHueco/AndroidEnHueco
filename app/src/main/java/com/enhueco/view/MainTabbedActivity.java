package com.enhueco.view;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.Toast;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.enhueco.R;
import com.enhueco.model.logicManagers.AccountManager;
import com.enhueco.model.logicManagers.FriendsManager;
import com.enhueco.model.logicManagers.privacyManager.PrivacyManager;
import com.enhueco.model.model.EnHueco;
import com.enhueco.model.model.User;
import com.enhueco.model.other.BasicCompletionListener;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

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

    @Bind(R.id.appbar) AppBarLayout appBarLayout;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    @Bind(R.id.container) ViewPager viewPager;

    private Menu optionsMenu;

    private ArrayList<Integer> hiddenMenuItems;
    @Bind(R.id.tabs) TabLayout tabLayout;

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
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main_tabbed, menu);

        optionsMenu = menu;

        menu.findItem(R.id.action_search).setVisible(false);

        if (tabLayout.getSelectedTabPosition() == 0)
        {
            menu.findItem(R.id.action_add_friend).setVisible(false);
            menu.findItem(R.id.action_requests).setVisible(false);
            menu.findItem(R.id.action_settings).setVisible(false);
        }

        if (tabLayout.getSelectedTabPosition() == 1)
        {
            menu.findItem(R.id.action_turn_invisible).setVisible(false);
            menu.findItem(R.id.action_im_available).setVisible(false);
            menu.findItem(R.id.action_settings).setVisible(false);
        }
        if (tabLayout.getSelectedTabPosition() == 2)
        {
            menu.findItem(R.id.action_turn_invisible).setVisible(false);
            menu.findItem(R.id.action_im_available).setVisible(false);
            menu.findItem(R.id.action_add_friend).setVisible(false);
            menu.findItem(R.id.action_requests).setVisible(false);
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

        AccountManager.getSharedManager().logout();
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
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


        if(resultCode == Activity.RESULT_CANCELED)
        {
            // Request cancelled
        }
        // Image Selection
        else if (requestCode == PICK_PHOTO_FOR_AVATAR && resultCode == Activity.RESULT_OK) {
            if (data == null) {
                //Display an error
                return;
            }
            try
            {
                InputStream inputStream = getApplicationContext().getContentResolver().openInputStream(data.getData());
            }
            catch (FileNotFoundException e)
            {
                e.printStackTrace();
            }
            //Now you can do whatever you want with your inpustream, save it as file, upload to a server, decode a bitmap...
        }

        // QR Code Scan Cancelled
        else if(result.getContents() == null) {
            Log.d("MainActivity", "Cancelled scan");
            Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
        }
        // QR Code Scan Succeed
        else
        {
            Log.d("MainActivity", "Scanned");
            try
            {
                User friend = FriendsManager.getSharedManager().addFriendFromStringEncodedFriendRepresentation(result.getContents());
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
        MainTabbedActivity.this.searchFriends();

/*
        AlertDialog.Builder addFriendMethodDialog = new AlertDialog.Builder(
                this);
        LayoutInflater factory = LayoutInflater.from(this);
        final View view = factory.inflate(R.layout.item_currently_available, null);

        List<DialogOption> data = new ArrayList<DialogOption>();
        data.add(new DialogOption("Buscar amigo", null));
        data.add(new DialogOption("Escanear código de amigo", null ));
        data.add(new DialogOption("Mostrar mi código", null));
        ListAdapter la = new DialogOption.DialogOptionArrayAdapter(this, 0, data);

        addFriendMethodDialog.setSingleChoiceItems(la, -1, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                MainTabbedActivity.this.searchFriends();


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

        addFriendMethodDialog.show();*/
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

    public void onViewMyQRButtonPressed (View view)
    {
        showQRCode();
    }

    public void onSettingsButtonPressed(MenuItem item)
    {
        startActivity(new Intent(this, SettingsActivity.class));
    }

    public void onTurnInvisibleButtonPressed(MenuItem item)
    {
        if (EnHueco.getInstance().getAppUser().isInvisible())
        {
            turnVisible();
        }
        else
        {
            turnInvisible();
        }
    }

    private void turnInvisible ()
    {
        AlertDialog.Builder addFriendMethodDialog = new AlertDialog.Builder(this);
        LayoutInflater factory = LayoutInflater.from(this);

        List<DialogOption> data = new ArrayList<>();
        data.add(new DialogOption("1:20 horas", null));
        data.add(new DialogOption("3 horas", null ));
        data.add(new DialogOption("Resto del día", null));
        ListAdapter la = new DialogOption.DialogOptionArrayAdapter(this, 0, data);

        addFriendMethodDialog.setSingleChoiceItems(la, -1, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                switch (which)
                {
                    case 0:
                        _turnInvisibleForInterval(80 * 60);
                        break;
                    case 1:
                        _turnInvisibleForInterval(3 * 60 * 60);
                        break;
                    case 2:

                        Calendar calendar = Calendar.getInstance();
                        calendar.add(Calendar.DAY_OF_MONTH, 1);
                        calendar.set(Calendar.HOUR_OF_DAY, 0);
                        calendar.set(Calendar.MINUTE, 0);
                        calendar.set(Calendar.SECOND, 0);
                        calendar.set(Calendar.MILLISECOND, 0);
                        int secondsUntilTomorrow = (int) ((calendar.getTimeInMillis()-System.currentTimeMillis())/1000);

                        _turnInvisibleForInterval(secondsUntilTomorrow);
                        break;
                }
                dialog.dismiss();
            }
        });

        addFriendMethodDialog.show();
    }

    private void turnVisible ()
    {
        PrivacyManager.getSharedManager().turnVisible(new BasicCompletionListener()
        {
            @Override
            public void onSuccess()
            {
                Drawable drawable = optionsMenu.findItem(R.id.action_turn_invisible).getIcon();

                if (drawable != null)
                {
                    drawable.mutate();
                    drawable.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
                    drawable.setAlpha(1);
                }
            }

            @Override
            public void onFailure(Exception error)
            {

            }
        });
    }

    private void _turnInvisibleForInterval(int seconds)
    {
        PrivacyManager.getSharedManager().turnInvisibleForTimeInterval(seconds, new BasicCompletionListener()
        {
            @Override
            public void onSuccess()
            {
                Drawable drawable = optionsMenu.findItem(R.id.action_turn_invisible).getIcon();

                if (drawable != null)
                {
                    drawable.mutate();
                    drawable.setColorFilter(Color.rgb(220, 170, 255), PorterDuff.Mode.SRC_ATOP);
                    drawable.setAlpha(1);
                }
            }

            @Override
            public void onFailure(Exception error)
            {

            }
        });
    }

    public void onImAvailableButtonPressed(MenuItem item)
    {
        InstantFreeTimeFragment fragment = InstantFreeTimeFragment.newInstance();
        fragment.show(getSupportFragmentManager(), "¡Estoy en Hueco!");
    }

    public void onProfileImagePressed(View view)
    {
        pickImage();
    }

    private static final int PICK_PHOTO_FOR_AVATAR = 0;

    public void pickImage() {
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
        public CharSequence getPageTitle(int position) { return tabNames[position]; }
    }
}
