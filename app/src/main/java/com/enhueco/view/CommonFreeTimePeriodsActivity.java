package com.enhueco.view;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;

import android.widget.Button;
import android.widget.SearchView;
import com.enhueco.model.logicManagers.ScheduleManager;
import com.enhueco.model.model.*;
import com.enhueco.model.model.EnHueco;
import com.enhueco.R;
import com.wefika.flowlayout.FlowLayout;

import java.util.ArrayList;
import java.util.List;

public class CommonFreeTimePeriodsActivity extends AppCompatActivity implements CommonFreeTimePeriodsSearchFriendToAddFragment.CommonFreeTimePeriodsSearchFriendToAddFragmentListener
{
    private SearchView searchView;

    CommonFreeTimePeriodsSearchFriendToAddFragment commonFreeTimePeriodsSearchFriendToAddFragment = new CommonFreeTimePeriodsSearchFriendToAddFragment();
    ScheduleFragment scheduleFragment = new ScheduleFragment();

    FlowLayout selectedFriendsFlowLayout;
    List<User> selectedFriends = new ArrayList<>();

    Fragment currentFragment = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        commonFreeTimePeriodsSearchFriendToAddFragment.setListener(this);

        setContentView(R.layout.activity_common_free_time_periods);

        searchView = (SearchView) findViewById(R.id.searchView);
        selectedFriendsFlowLayout = (FlowLayout) findViewById(R.id.selectedFriendsFlowLayout);

        addFriendToSelectedFriendsAndReloadData(EnHueco.getInstance().getAppUser());

        for (User user: EnHueco.getInstance().getAppUser().getFriends().values())
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
                commonFreeTimePeriodsSearchFriendToAddFragment.filterContentForSearchText(newText);
                return false;
            }
        });

        switchToCalendar();
    }


    public void prepareInfoAndReloadScheduleData ()
    {
        // TODO: Implement Common Free Time Periods
        /*
        Schedule commonFreeTimePeriodsSchedule = ScheduleManager.getSharedManager().getCommonFreeTimePeriodsScheduleForUsers(selectedFriends.toArray(new User[0]));
        scheduleFragment.setSchedule(commonFreeTimePeriodsSchedule);
        scheduleFragment.reloadData();
        */
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
        if (currentFragment == commonFreeTimePeriodsSearchFriendToAddFragment) return;

        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        fragmentTransaction.replace(R.id.fragment_container, commonFreeTimePeriodsSearchFriendToAddFragment);
        fragmentTransaction.addToBackStack(null);

        currentFragment = commonFreeTimePeriodsSearchFriendToAddFragment;

        findViewById(R.id.selectedFriendsScrollView).setVisibility(View.GONE);

        fragmentTransaction.commit();
    }

    public void reloadSelectedFriendsView ()
    {
        selectedFriendsFlowLayout.removeAllViews();

        for (User user: selectedFriends)
        {
            View cell = LayoutInflater.from(this).inflate(R.layout.item_common_free_time_periods_selected_friend, null);
            ((Button) cell.findViewById(R.id.commonFreeTimePeriodsSelectedFriendButton)).setText(user.getName());
            selectedFriendsFlowLayout.addView(cell);
        }
    }

    @Override
    public void onCommonFreeTimePeriodsSearchFriendToAddFragmentNewFriendSelected(User friend)
    {
        addFriendToSelectedFriendsAndReloadData(friend);
    }
}
