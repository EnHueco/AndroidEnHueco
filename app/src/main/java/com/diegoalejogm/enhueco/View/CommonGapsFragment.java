package com.diegoalejogm.enhueco.View;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.SearchView;
import com.diegoalejogm.enhueco.R;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link CommonGapsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class CommonGapsFragment extends Fragment
{
    private OnFragmentInteractionListener mListener;

    private SearchView searchView;

    CommonGapsSearchFriendToAddFragment commonGapsSearchFriendToAddFragment = new CommonGapsSearchFriendToAddFragment();

    public CommonGapsFragment()
    {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_common_gaps, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        searchView = (SearchView) getView().findViewById(R.id.searchView);

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
    }

    public void prepareInfoAndReloadScheduleData ()
    {

    }

    public void addFriendToSelectedFriendsAndReloadData ()
    {

    }

    public void switchToCalendar ()
    {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        CommonGapsSearchFriendToAddFragment commonGapsSearchFriendToAddFragment = new CommonGapsSearchFriendToAddFragment();

        fragmentTransaction.replace(R.id.fragment_container, commonGapsSearchFriendToAddFragment);
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
