package com.diegoalejogm.enhueco.View;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.diegoalejogm.enhueco.Model.MainClasses.*;
import com.diegoalejogm.enhueco.Model.MainClasses.System;
import com.diegoalejogm.enhueco.Model.Other.Tuple;
import com.diegoalejogm.enhueco.R;

import java.text.DecimalFormat;
import java.util.*;

/**
 * A fragment representing a list of Items.
 * <p/>
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnFragmentInteractionListener}
 * interface.
 */
public class InGapFragment extends ListFragment
{

    private OnFragmentInteractionListener mListener;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public InGapFragment()
    {
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // TODO: Change Adapter to display your content

        List<Tuple<User, Event>> data = System.instance.getAppUser().getFriendsCurrentlyInGap();

        setListAdapter(new InGapArrayAdapter(getActivity(),
                0, data));
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
            // Notify the active callbacks interface (the activity, if the
            // fragment is attached to one) that an item has been selected.
//            mListener.onFragmentInteraction(DummyContent.ITEMS.get(position).id);
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
        // TODO: Update argument type and name
        public void onFragmentInteraction(String id);
    }


    static class InGapArrayAdapter extends ArrayAdapter<Tuple<User, Event>>
    {

        Context context;
        List<Tuple<User, Event>> objects;

        public InGapArrayAdapter(Context context, int resource, List<Tuple<User, Event>> objects)
        {
            super(context, resource, objects);
            this.context = context;
            this.objects = objects;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            User user = objects.get(position).first;
            Event event = objects.get(position).second;

            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

            View view = inflater.inflate(R.layout.item_enhueco, null);
            TextView tv1 = (TextView) view.findViewById(R.id.enHuecoItem_nameTextView);
            tv1.setText(user.toString());

            TextView tv2 = (TextView) view.findViewById(R.id.gapRemainingTimeText);
            Calendar localTime = Calendar.getInstance(TimeZone.getTimeZone("UTC"));

            int remainingMinutes = (event.getEndHour().get(Calendar.HOUR_OF_DAY) * 60 + event.getEndHour().get(Calendar.MINUTE))
                    - (localTime.get(Calendar.HOUR_OF_DAY) * 60 + localTime.get(Calendar.MINUTE));

            Log.v("InGapFragment", remainingMinutes+"");
            int remainingHour = remainingMinutes / 60;
            remainingMinutes -= remainingHour * 60;

            DecimalFormat mFormat = new DecimalFormat("00");


            String timeRemaining = remainingHour > 0 ? mFormat.format(remainingHour) + ":" + mFormat.format(remainingMinutes) + " horas" :
                    mFormat.format(remainingMinutes) + " min";
            tv2.setText(timeRemaining);
            return view;
        }
    }

}
