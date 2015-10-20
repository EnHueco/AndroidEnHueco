package com.diegoalejogm.enhueco.View;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.diegoalejogm.enhueco.Model.MainClasses.User;
import com.diegoalejogm.enhueco.R;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.zip.Inflater;

import com.diegoalejogm.enhueco.Model.MainClasses.System;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

/**
 * A fragment representing a list of Items.
 * <p/>
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnFragmentInteractionListener}
 * interface.
 */
public class FriendsFragment extends ListFragment
{

    private OnFragmentInteractionListener mListener;
    FriendsArrayAdapter friendArrayAdapter;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public FriendsFragment()
    {
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        friendArrayAdapter = new FriendsArrayAdapter(getActivity(), 0, System.instance.getAppUser().getFriends());
        setListAdapter(friendArrayAdapter);
    }

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        try
        {
            mListener = (OnFragmentInteractionListener) activity;
        }
        catch (ClassCastException e)
        {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }


    @Override
    public void onDetach()
    {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id)
    {
        super.onListItemClick(l, v, position, id);

        if (null != mListener)
        {
            Intent intent = new Intent(getActivity(), FriendDetailActivity.class);
            intent.putExtra("friendID", System.instance.getAppUser().getFriends().get(position).getID());
            startActivity(intent);
        }
    }

    public void refresh()
    {
        friendArrayAdapter.notifyDataSetChanged();
    }

    @Override
    public void onResume()
    {
        super.onResume();
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener
    {
    }

    public class FriendsArrayAdapter extends ArrayAdapter<User>
    {

        Context context;
        List<User> objects;
        public FriendsArrayAdapter(Context context, int resource, List<User> objects)
        {
            super(context, resource, objects);
            this.context = context;
            this.objects = objects;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.item_friend, null);

            TextView tv1 = (TextView) view.findViewById(R.id.enHuecoFriend_nameTextView);
            tv1.setText(objects.get(position).toString());

            return view;
        }
    }

}
