package com.diegoalejogm.enhueco.View;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.os.PersistableBundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.SearchView;
import com.diegoalejogm.enhueco.Model.MainClasses.*;
import com.diegoalejogm.enhueco.Model.MainClasses.System;
import com.diegoalejogm.enhueco.R;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link CommonGapsActivity.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class CommonGapsActivity extends Activity
{
    private OnFragmentInteractionListener mListener;

    private SearchView searchView;

    CommonGapsSearchFriendToAddFragment commonGapsSearchFriendToAddFragment = new CommonGapsSearchFriendToAddFragment();
    ScheduleFragment scheduleFragment = new ScheduleFragment();

    List<User> selectedFriends = new ArrayList<>();

    public CommonGapsActivity()
    {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState)
    {
        super.onCreate(savedInstanceState, persistentState);

        setContentView(R.layout.activity_common_gaps);

        searchView = (SearchView) findViewById(R.id.searchView);

        searchView.setOnFocusChangeListener(new View.OnFocusChangeListener()
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
        addFriendToSelectedFriendsAndReloadData(System.instance.getAppUser());
    }

    public void prepareInfoAndReloadScheduleData ()
    {

    }

    public void addFriendToSelectedFriendsAndReloadData (User friend)
    {
        if (friend.getClass() == AppUser.class)
        {
            selectedFriends.set(0, friend);
        }
        else
        {
            selectedFriends.add(friend);
        }

        prepareInfoAndReloadScheduleData();
    }

    public void switchToCalendar ()
    {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        fragmentTransaction.replace(R.id.fragment_container, scheduleFragment);
        fragmentTransaction.addToBackStack(null);

        fragmentTransaction.commit();
    }

    public void switchToSearch ()
    {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        fragmentTransaction.replace(R.id.fragment_container, commonGapsSearchFriendToAddFragment);
        fragmentTransaction.addToBackStack(null);

        fragmentTransaction.commit();
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri)
    {
        if (mListener != null)
        {
            mListener.onFragmentInteraction(uri);
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener
    {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

}
