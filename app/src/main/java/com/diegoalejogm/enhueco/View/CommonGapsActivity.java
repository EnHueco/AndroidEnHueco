package com.diegoalejogm.enhueco.View;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;

import android.widget.Button;
import android.widget.SearchView;
import com.diegoalejogm.enhueco.Model.MainClasses.*;
import com.diegoalejogm.enhueco.Model.MainClasses.System;
import com.diegoalejogm.enhueco.R;
import com.wefika.flowlayout.FlowLayout;

import java.util.ArrayList;
import java.util.List;

public class CommonGapsActivity extends AppCompatActivity implements CommonGapsSearchFriendToAddFragment.CommonGapsSearchFriendToAddFragmentListener
{
    private SearchView searchView;

    CommonGapsSearchFriendToAddFragment commonGapsSearchFriendToAddFragment = new CommonGapsSearchFriendToAddFragment();
    ScheduleFragment scheduleFragment = new ScheduleFragment();

    FlowLayout selectedFriendsFlowLayout;
    List<User> selectedFriends = new ArrayList<>();

    Fragment currentFragment = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        commonGapsSearchFriendToAddFragment.setListener(this);

        setContentView(R.layout.activity_common_gaps);

        searchView = (SearchView) findViewById(R.id.searchView);
        selectedFriendsFlowLayout = (FlowLayout) findViewById(R.id.selectedFriendsFlowLayout);

        addFriendToSelectedFriendsAndReloadData(System.getInstance().getAppUser());

        for (User user: System.getInstance().getAppUser().getFriends())
        {
            if (user.getID().equals(getIntent().getStringExtra("initialFriendID")))
            {
                addFriendToSelectedFriendsAndReloadData(user);
                break;
            }
        }

        searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener()
        {
            @Override
            public void onFocusChange(View v, boolean hasFocus)
            {
                if (hasFocus) switchToSearch(); else switchToCalendar();
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener()
        {
            @Override
            public boolean onQueryTextSubmit(String query)
            {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText)
            {
                commonGapsSearchFriendToAddFragment.filterContentForSearchText(newText);
                return false;
            }
        });

        switchToCalendar();
    }


    public void prepareInfoAndReloadScheduleData ()
    {
        Schedule commonGapsSchedule = System.getInstance().getAppUser().getCommonGapsScheduleForUsers(selectedFriends.toArray(new User[0]));
        scheduleFragment.setSchedule(commonGapsSchedule);
        scheduleFragment.reloadData();
    }

    public void addFriendToSelectedFriendsAndReloadData (User friend)
    {
        selectedFriends.add(friend);
        reloadSelectedFriendsView();
        prepareInfoAndReloadScheduleData();
    }

    public void switchToCalendar ()
    {
        if (currentFragment == scheduleFragment) return;

        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        fragmentTransaction.replace(R.id.fragment_container, scheduleFragment);
        fragmentTransaction.addToBackStack(null);

        currentFragment = scheduleFragment;

        findViewById(R.id.selectedFriendsScrollView).setVisibility(View.VISIBLE);

        fragmentTransaction.commit();
    }

    public void switchToSearch ()
    {
        if (currentFragment == commonGapsSearchFriendToAddFragment) return;

        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        fragmentTransaction.replace(R.id.fragment_container, commonGapsSearchFriendToAddFragment);
        fragmentTransaction.addToBackStack(null);

        currentFragment = commonGapsSearchFriendToAddFragment;

        findViewById(R.id.selectedFriendsScrollView).setVisibility(View.GONE);

        fragmentTransaction.commit();
    }

    public void reloadSelectedFriendsView ()
    {
        selectedFriendsFlowLayout.removeAllViews();

        for (User user: selectedFriends)
        {
            View cell = LayoutInflater.from(this).inflate(R.layout.item_common_gaps_selected_friend, null);
            ((Button) cell.findViewById(R.id.commonGapsSelectedFriendButton)).setText(user.getName());
            selectedFriendsFlowLayout.addView(cell);
        }
    }

    @Override
    public void onCommonGapsSearchFriendToAddFragmentNewFriendSelected(User friend)
    {
        addFriendToSelectedFriendsAndReloadData(friend);
    }
}
