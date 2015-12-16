package com.diegoalejogm.enhueco.view;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.ListFragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.diegoalejogm.enhueco.Model.MainClasses.Event;
import com.diegoalejogm.enhueco.Model.MainClasses.System;
import com.diegoalejogm.enhueco.Model.Other.EHURLS;
import com.diegoalejogm.enhueco.Model.MainClasses.User;
import com.diegoalejogm.enhueco.Model.Other.Tuple;
import com.diegoalejogm.enhueco.R;
import com.makeramen.roundedimageview.RoundedTransformationBuilder;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

/**
 * A fragment representing a list of Items.
 * <p/>
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnFragmentInteractionListener}
 * interface.
 */
public class CurrentlyAvailableFragment extends ListFragment
{

    private OnFragmentInteractionListener mListener;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public CurrentlyAvailableFragment()
    {
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // TODO: Change Adapter to display your content


        List<Tuple<User, Event>> data = new ArrayList<>();

        System.getInstance().getAppUser().getCurrentlyAvailableFriends();



        setListAdapter(new CurrentlyFreeArrayAdapter(getActivity(),
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
    public void onResume()
    {
        super.onResume();

        AppBarLayout appBarLayout = ((MainTabbedActivity) getActivity()).getAppBarLayout();
        appBarLayout.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser)
    {
        super.setUserVisibleHint(isVisibleToUser);

        if (isVisibleToUser)
        {
            //((MainTabbedActivity) getActivity()).getWindow().setStatusBarColor(getResources().getColor(R.color.mb_gray)=;
            final AppBarLayout appBarLayout = ((MainTabbedActivity) getActivity()).getAppBarLayout();

            Integer colorFrom = ((ColorDrawable)appBarLayout.getBackground()).getColor();
            Integer colorTo = ContextCompat.getColor(getContext(), R.color.colorPrimary);
            ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
            colorAnimation.setDuration(400);

            colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener()
            {
                @Override
                public void onAnimationUpdate(ValueAnimator animator)
                {
                    appBarLayout.setBackgroundColor((Integer)animator.getAnimatedValue());
                }
            });
            colorAnimation.start();

            refresh();
            System.getInstance().getAppUser().fetchUpdatesForFriendsAndFriendSchedules();
        }
    }



    private void refresh()
    {
        List<Tuple<User, Event>> data = System.getInstance().getAppUser().getCurrentlyAvailableFriends();
        setListAdapter(new CurrentlyFreeArrayAdapter(getActivity(),
                0, data));
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

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        setEmptyText("No tienes amigos en hueco");
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


    static class CurrentlyFreeArrayAdapter extends ArrayAdapter<Tuple<User, Event>>
    {

        Context context;
        List<Tuple<User, Event>> objects;

        public CurrentlyFreeArrayAdapter(Context context, int resource, List<Tuple<User, Event>> objects)
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

            View view = inflater.inflate(R.layout.item_currently_available, null);

            ImageView iv = (ImageView) view.findViewById(R.id.friendIcon);
            Transformation transformation = new RoundedTransformationBuilder().oval(true).build();

            Picasso.with(context).load(EHURLS.BASE + user.getImageURL().get()).fit().transform(transformation).into(iv);

            TextView tv1 = (TextView) view.findViewById(R.id.nameTextView);
            tv1.setText(user.toString());

            TextView tv2 = (TextView) view.findViewById(R.id.freeTimeEndTime);
            Calendar localTime = event.getEndHourCalendarInLocalTimezone();

            DecimalFormat mFormat = new DecimalFormat("00");

            int hour = localTime.get(Calendar.HOUR_OF_DAY);
            String ampm = "AM";
            if(localTime.get(Calendar.HOUR_OF_DAY) > 12)
            {
                hour-=12; ampm = "PM";
            }

            String timeRemaining = mFormat.format(hour) + ":"
                    + mFormat.format(localTime.get(Calendar.MINUTE)) + " " + ampm;
            tv2.setText(timeRemaining);
            return view;
        }
    }

}
