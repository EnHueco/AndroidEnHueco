package com.diegoalejogm.enhueco.view;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.diegoalejogm.enhueco.Model.MainClasses.Event;
import com.diegoalejogm.enhueco.Model.MainClasses.User;
import com.diegoalejogm.enhueco.Model.Other.EHURLS;
import com.diegoalejogm.enhueco.R;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.List;

import com.diegoalejogm.enhueco.Model.MainClasses.System;
import com.makeramen.roundedimageview.RoundedTransformationBuilder;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

/**
 * A fragment representing a list of Items.
 * <p/>
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnFragmentInteractionListener}
 * interface.
 */
public class FriendListFragment extends ListFragment
{

    private static final String LOG = "FriendListFragment";
    private OnFragmentInteractionListener mListener;
    private FriendsArrayAdapter friendArrayAdapter;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public FriendListFragment()
    {
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);




        friendArrayAdapter = new FriendsArrayAdapter(getActivity(), 0, System.getInstance().getAppUser().getFriends());
        setListAdapter(friendArrayAdapter);

        LocalBroadcastManager.getInstance(getContext()).registerReceiver(new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                Log.v(LOG, System.EHSystemNotification.SYSTEM_DID_RECEIVE_FRIEND_AND_SCHEDULE_UPDATES);
                refresh();
            }
        }, new IntentFilter(System.EHSystemNotification.SYSTEM_DID_RECEIVE_FRIEND_AND_SCHEDULE_UPDATES));

        LocalBroadcastManager.getInstance(getContext()).registerReceiver(new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                Log.v(LOG, System.EHSystemNotification.SYSTEM_DID_RECEIVE_FRIEND_DELETION);
                refresh();
            }
        }, new IntentFilter(System.EHSystemNotification.SYSTEM_DID_RECEIVE_FRIEND_DELETION));

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
            intent.putExtra("friendID", System.getInstance().getAppUser().getFriends().get(position).getID());
            startActivity(intent);
        }
    }

    public void refresh()
    {
        friendArrayAdapter.notifyDataSetChanged();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        setEmptyText("No tienes amigos. \n Selecciona AGREGAR para agregar uno");
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser)
    {
        super.setUserVisibleHint(isVisibleToUser);

        if (isVisibleToUser)
        {
            refresh();
            System.getInstance().getAppUser().fetchUpdatesForFriendsAndFriendSchedules();
        }
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
            User user = objects.get(position);
            Event eventShown = user.currentGap();
            if(eventShown == null) eventShown = user.nextFreeTimePeriod();

            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.item_friend, null);

            ImageView iv = (ImageView) view.findViewById(R.id.friendIcon);
            Transformation transformation = new RoundedTransformationBuilder().oval(true).build();

            Picasso.with(context).load(EHURLS.BASE + user.getImageURL().get()).fit().transform(transformation).into(iv);

            TextView tv1 = (TextView) view.findViewById(R.id.nameTextView);
            tv1.setText(user.toString());

            TextView nextFreeTimeHourTextView = (TextView) view.findViewById(R.id.nextFreeTime);
            TextView nextFreeTimeHourNameTextView = (TextView) view.findViewById(R.id.nextFreeTimeName);

            if(eventShown != null)
            {
                DecimalFormat mFormat = new DecimalFormat("00");
                String postFix = "AM";

                int hour = eventShown.getStartHourCalendarInLocalTimezone().get(Calendar.HOUR_OF_DAY);
                int minute = eventShown.getStartHour().get(Calendar.MINUTE);
                if(hour > 12)
                {
                    hour-=12;
                    postFix = "PM";
                }
                String time = mFormat.format(hour) +  ":" + mFormat.format(minute) + " " + postFix;

                nextFreeTimeHourTextView.setText(time);
                nextFreeTimeHourNameTextView.setText(eventShown.getName().or(""));
            }
            else
            {
                nextFreeTimeHourTextView.setText("-- --");
                nextFreeTimeHourNameTextView.setText("");
            }

            return view;
        }


    }
}
