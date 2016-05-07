package com.enhueco.view;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.ListFragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.enhueco.R;
import com.enhueco.model.logicManagers.AppUserInformationManager;
import com.enhueco.model.logicManagers.FriendsInformationManager;
import com.enhueco.model.logicManagers.CurrentStateManager.CurrentStateManager;
import com.enhueco.model.logicManagers.CurrentStateManager.CurrentStateManagerNotification;
import com.enhueco.model.model.EnHueco;
import com.enhueco.model.model.Event;
import com.enhueco.model.model.User;
import com.enhueco.model.model.immediateEvent.ImmediateEvent;
import com.enhueco.model.other.BasicCompletionListener;
import com.enhueco.model.other.EHURLS;
import com.enhueco.model.other.Utilities;
import com.enhueco.model.structures.Tuple;
import com.google.common.base.Optional;
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

    final List<Tuple<User, Event>> currentlyAvailableFriends = new ArrayList<>();
    CurrentlyFreeArrayAdapter adapter;

    private static final String LOG = "CurrAvailableFragment";


    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public CurrentlyAvailableFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        currentlyAvailableFriends.clear();
        currentlyAvailableFriends.addAll(CurrentStateManager.getSharedManager().getCurrentlyAvailableFriends());

        Optional<ImmediateEvent> instantFreeTimePeriod = EnHueco.getInstance().getAppUser().getInstantFreeTimePeriod();

        if (instantFreeTimePeriod.isPresent() && instantFreeTimePeriod.get().getType().equals(ImmediateEvent.ImmediateEventType.EVENT))
        {
            currentlyAvailableFriends.add(0, new Tuple<>((User) EnHueco.getInstance().getAppUser(), new Event(instantFreeTimePeriod.get())));
        }

        adapter = new CurrentlyFreeArrayAdapter(getActivity(), 0, currentlyAvailableFriends);
        setListAdapter(adapter);

        LocalBroadcastManager.getInstance(getContext()).registerReceiver(new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                refresh();
            }
        }, new IntentFilter(EnHueco.EHSystemNotification.SYSTEM_DID_RECEIVE_FRIEND_AND_SCHEDULE_UPDATES));

        LocalBroadcastManager.getInstance(getContext()).registerReceiver(new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                refresh();
            }
        }, new IntentFilter(CurrentStateManagerNotification.DID_POST_INSTANT_FREE_TIME_PERIOD));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        registerForContextMenu(view);

        return view;
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
            /*
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
                    appBarLayout.setBackgroundColor((Integer) animator.getAnimatedValue());
                }
            });
            colorAnimation.start();

*/
            refresh();

            FriendsInformationManager.getSharedManager().fetchUpdatesForFriendsAndFriendSchedules(new BasicCompletionListener()
            {
                @Override
                public void onSuccess()
                {
                    refresh();
                }

                @Override
                public void onFailure(Exception error)
                {

                }
            });
            AppUserInformationManager.getSharedManager().fetchUpdatesForAppUserAndSchedule(new BasicCompletionListener()
            {
                @Override
                public void onSuccess()
                {
                    refresh();
                }

                @Override
                public void onFailure(Exception error)
                {

                }
            });
        }
    }

    private void refresh()
    {
        currentlyAvailableFriends.clear();

        // Add user immediate event if currently happening
        Optional<ImmediateEvent> instantFreeTimePeriod = EnHueco.getInstance().getAppUser().getInstantFreeTimePeriod();

            if (instantFreeTimePeriod.isPresent() &&
                instantFreeTimePeriod.get().getEndHour().compareTo(Calendar.getInstance(TimeZone.getTimeZone("UTC"))) >= 0 &&
                instantFreeTimePeriod.get().getType().equals(ImmediateEvent.ImmediateEventType.EVENT))
        {
            currentlyAvailableFriends.add(0, new Tuple<>((User) EnHueco.getInstance().getAppUser(), new Event(instantFreeTimePeriod.get())));
        }

        // Add friends
        List<Tuple<User, Event>> friends = CurrentStateManager.getSharedManager().getCurrentlyAvailableFriends();
        currentlyAvailableFriends.addAll(friends);

        adapter.notifyDataSetChanged();
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
            Optional<ImmediateEvent> instantFreeTimePeriod = EnHueco.getInstance().getAppUser().getInstantFreeTimePeriod();
            boolean instantFreeTimeActive = instantFreeTimePeriod.isPresent() &&
                    instantFreeTimePeriod.get().getEndHour().compareTo(Calendar.getInstance(TimeZone.getTimeZone("UTC"))) >= 0 &&
                    instantFreeTimePeriod.get().getType().equals(ImmediateEvent.ImmediateEventType.EVENT);

            if(instantFreeTimeActive && position == 0) return;
            Intent intent = new Intent(getActivity(), FriendDetailActivity.class);
            intent.putExtra("friendID", currentlyAvailableFriends.get(position).first.getID());
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

            Transformation transformation = Utilities.getRoundTransformation();
            if (user.getImageURL().isPresent())
            {
                Picasso.with(context).load(EHURLS.BASE + user.getImageThumbnail()).fit().transform(transformation).into
                        (iv);
            }

            TextView tv1 = (TextView) view.findViewById(R.id.nameTextView);
            tv1.setText(user.toString());

            TextView tv2 = (TextView) view.findViewById(R.id.freeTimeEndTime);
            Calendar localTime = event.getEndHourCalendarInLocalTimezone();

            TextView tvFreeTimeName= (TextView) view.findViewById(R.id.freeTimeName);
            tvFreeTimeName.setText(event.getName().or(""));

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
