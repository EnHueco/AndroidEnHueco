package com.enhueco.view;

import android.app.Activity;
import android.content.*;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.ListFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.*;
import android.widget.*;
import com.enhueco.R;
import com.enhueco.model.logicManagers.AppUserInformationManager;
import com.enhueco.model.logicManagers.FriendsInformationManager;
import com.enhueco.model.logicManagers.CurrentStateManager.CurrentStateManager;
import com.enhueco.model.logicManagers.ImmediateEventManager;
import com.enhueco.model.model.AppUser;
import com.enhueco.model.model.EnHueco;
import com.enhueco.model.model.Event;
import com.enhueco.model.model.User;
import com.enhueco.model.model.immediateEvent.ImmediateEvent;
import com.enhueco.model.other.BasicCompletionListener;
import com.enhueco.model.other.EHURLS;
import com.enhueco.model.other.Utilities;
import com.enhueco.model.structures.Tuple;
import com.enhueco.view.dialog.EHProgressDialog;
import com.google.common.base.Optional;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.*;

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
    public CurrentlyAvailableFragment()
    {
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        currentlyAvailableFriends.clear();
        currentlyAvailableFriends.addAll(CurrentStateManager.getSharedManager().getCurrentlyAvailableFriends());

        Optional<ImmediateEvent> instantFreeTimePeriod = EnHueco.getInstance().getAppUser().getImmediateEvent();

        if (instantFreeTimePeriod.isPresent() && instantFreeTimePeriod.get().getType().equals(ImmediateEvent.ImmediateEventType.EVENT))
        {
            currentlyAvailableFriends.add(0, new Tuple<>((User) EnHueco.getInstance().getAppUser(), new Event(instantFreeTimePeriod.get())));
        }

        adapter = new CurrentlyFreeArrayAdapter(getActivity(), 0, currentlyAvailableFriends);
        setListAdapter(adapter);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.menu_item_switch_visibility:
                switchVisibility();
                break;
            case R.id.menu_item_im_available:
                switchImAvailable();
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    private void switchImAvailable()
    {
        ImmediateEvent event = EnHueco.getInstance().getAppUser().getImmediateEvent().get();

        if (EnHueco.getInstance().getAppUser().getCurrentFreeTimePeriod().isPresent())
        {
            Toast.makeText(this.getContext(), "Ya te encuentras en hueco", Toast.LENGTH_SHORT).show();
        }

        else if (event.isCurrentlyHappening() && event.getType().equals(ImmediateEvent.ImmediateEventType.EVENT))
        {
            final EHProgressDialog dialog = new EHProgressDialog(this.getContext());
            dialog.show();
            ImmediateEventManager.getSharedManager().deleteInstantFreeTimeEvent(new BasicCompletionListener()
            {
                @Override
                public void onSuccess()
                {
                    dialog.dismiss();
                    refresh();
                }

                @Override
                public void onFailure(Exception error)
                {
                    dialog.dismiss();
                    Utilities.showErrorToast(CurrentlyAvailableFragment.this.getContext());
                }
            });
        }
        else
        {
            InstantFreeTimeFragment fragment = InstantFreeTimeFragment.newInstance();
            fragment.setTargetFragment(this, InstantFreeTimeFragment.resultCode);
            fragment.show(getActivity().getSupportFragmentManager(), "¡Estoy en Hueco!");
        }
    }

    public void switchVisibility()
    {
        ImmediateEvent event = EnHueco.getInstance().getAppUser().getImmediateEvent().get();
        if (event.isCurrentlyHappening() && event.getType().equals(ImmediateEvent.ImmediateEventType.INVISIBILITY))
        {
            turnVisible();
        }
        else
        {
            turnInvisible();
        }
    }

    private void turnVisible()
    {
        final EHProgressDialog dialog = new EHProgressDialog(this.getContext());
        dialog.show();
        ImmediateEventManager.getSharedManager().turnVisible(new BasicCompletionListener()
        {
            @Override
            public void onSuccess()
            {
                dialog.dismiss();
                refresh();
            }

            @Override
            public void onFailure(Exception error)
            {
                dialog.dismiss();
                Utilities.showErrorToast(CurrentlyAvailableFragment.this.getContext());
            }
        });
    }

    private void turnInvisible()
    {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this.getContext());
        int selectedOption = -1;
        CharSequence[] items = {"1:20 horas", "3 horas", "Resto del día"};
        final int[] selectedItemTime = {90, 180, Utilities.getSecondsUntilTomorrow()};


        alertDialog.setTitle("Duración").setSingleChoiceItems(items, 0, null).setPositiveButton("Aceptar", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                int position = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                _turnInvisibleForInterval(selectedItemTime[position]);
                dialog.dismiss();
            }
        }).setNegativeButton("Cancelar", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.dismiss();
            }
        });
        alertDialog.show();
    }

    private void _turnInvisibleForInterval(int seconds)
    {
        final EHProgressDialog dialog = new EHProgressDialog(this.getContext());
        dialog.show();
        ImmediateEventManager.getSharedManager().turnInvisibleForTimeInterval(seconds, new BasicCompletionListener()
        {
            @Override
            public void onSuccess()
            {
                dialog.dismiss();
                refresh();
            }

            @Override
            public void onFailure(Exception error)
            {
                dialog.dismiss();
                Utilities.showErrorToast(CurrentlyAvailableFragment.this.getContext());
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        registerForContextMenu(view);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        inflater.inflate(R.menu.menu_available_friends, menu);
        super.onCreateOptionsMenu(menu, inflater);
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
        CurrentlyAvailableFragment.this.getActivity().runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                currentlyAvailableFriends.clear();

                // Add user immediate event if currently happening
                AppUser appUser = EnHueco.getInstance().getAppUser();
                Optional<ImmediateEvent> instantFreeTimePeriod = appUser.getImmediateEvent();

                if (appUser.isInInstantFreeTime())
                {
                    currentlyAvailableFriends.add(0, new Tuple<>((User) EnHueco.getInstance().getAppUser(), new Event(instantFreeTimePeriod.get())));
                }
                // Add friends
                List<Tuple<User, Event>> friends = CurrentStateManager.getSharedManager().getCurrentlyAvailableFriends();
                currentlyAvailableFriends.addAll(friends);

                adapter.notifyDataSetChanged();
            }
        });

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
            Optional<ImmediateEvent> instantFreeTimePeriod = EnHueco.getInstance().getAppUser().getImmediateEvent();
            boolean instantFreeTimeActive = instantFreeTimePeriod.isPresent() && instantFreeTimePeriod.get()
                    .isCurrentlyHappening() && instantFreeTimePeriod.get().getType().equals(ImmediateEvent
                    .ImmediateEventType.EVENT);

            if (instantFreeTimeActive && position == 0) return;
            Intent intent = new Intent(getActivity(), FriendDetailActivity.class);
            intent.putExtra("friendID", currentlyAvailableFriends.get(position).first.getID());
            startActivity(intent);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        // Stuff to do, dependent on requestCode and resultCode
        if (requestCode == InstantFreeTimeFragment.resultCode && resultCode == InstantFreeTimeFragment.resultCode)
        {
            // This is the return result of your DialogFragment
            refresh();
        }
        super.onActivityResult(requestCode, resultCode, data);
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


            TextView tvFreeTimeName = (TextView) view.findViewById(R.id.freeTimeName);
            tvFreeTimeName.setText(event.getName().or(""));

            TextView tv2 = (TextView) view.findViewById(R.id.freeTimeEndTime);

            DateTime startDateTime = event.getStartHourInLocalTimezone().toDateTimeToday(DateTimeZone.UTC);
            DateTime endDateTime = event.getEndHourInLocalTimezone().toDateTimeToday(DateTimeZone.UTC);
            DateTimeFormatter dtf = DateTimeFormat.forPattern("hh:mm a");

            tv2.setText(dtf.print(startDateTime) + " - " + dtf.print(endDateTime));
            return view;
        }
    }

}
