package com.enhueco.model.logicManagers.CurrentStateManager;

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.LocalBroadcastManager;
import com.enhueco.model.EHApplication;
import com.enhueco.model.logicManagers.ProximityUpdatesManager;
import com.enhueco.model.logicManagers.genericManagers.connectionManager.*;
import com.enhueco.model.model.EnHueco;
import com.enhueco.model.model.Event;
import com.enhueco.model.model.User;
import com.enhueco.model.other.BasicCompletionListener;
import com.enhueco.model.other.EHURLS;
import com.enhueco.model.structures.Tuple;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * Created by Diego on 2/28/16.
 */
public class CurrentStateManager
{
    private static CurrentStateManager instance;

    public static CurrentStateManager getSharedManager()
    {
        if (instance == null)
        {
            instance = new CurrentStateManager();
        }

        return instance;
    }

    /**
     * Returns friends who are currently nearby and for who the app user has not been notified for
     * a time longer than ProximityUpdatesManager.MINIMUM_TIME_INTERVAL_BETWEEN_NOTIFICATIONS
     */
    public Collection<User> getFriendsCurrentlyNearbyAndEligibleForNotification()
    {
        return Collections2.filter(EnHueco.getInstance().getAppUser().getFriends().values(), new Predicate<User>()
        {
            @Override
            public boolean apply(User friend)
            {
                return friend.isNearby()
                        && (!friend.getLastNotifiedNearbyStatusDate().isPresent()
                        || new Date().getTime() - friend.getLastNotifiedNearbyStatusDate().get().getTime() > ProximityUpdatesManager.MINIMUM_TIME_INTERVAL_BETWEEN_NOTIFICATIONS);
            }
        });
    }

    /**
     * Returns all friends that are currently available.
     *
     * @return Friends with their current free time period
     */
    public List<Tuple<User, Event>> getCurrentlyAvailableFriends()
    {
        List<Tuple<User, Event>> friendsAndFreeTimePeriods = new ArrayList<>();

        for (User friend : EnHueco.getInstance().getAppUser().getFriends().values())
        {
            Optional<Event> currentFreeTimePeriod = friend.getCurrentFreeTimePeriod();

            if (currentFreeTimePeriod.isPresent())
            {
                friendsAndFreeTimePeriods.add(new Tuple<>(friend, currentFreeTimePeriod.get()));
            }
        }

        return friendsAndFreeTimePeriods;
    }

    /**
     * Posts an instant free time period that everyone sees and that overrides any classes present in the app user's schedule during the instant free time period duration.
     * Network operation must succeed immediately or else the newFreeTimePeriod is discarded.
     *
     * @param newFreeTimePeriod Event that represents the free time period to be posted
     */
    public void postInstantFreeTimePeriod(final Event newFreeTimePeriod, final BasicCompletionListener completionListener)
    {
        JSONObject params = new JSONObject();

        try
        {
            JSONObject instantEvent = new JSONObject();

            instantEvent.put("type", "EVENT");
            instantEvent.put("valid_until", newFreeTimePeriod.getEndHourInDate(new Date()));
            instantEvent.put("name", newFreeTimePeriod.getName().orNull());
            instantEvent.put("location", newFreeTimePeriod.getLocation().orNull());

            params.put("immediate_event", instantEvent);
        }
        catch (JSONException e) { e.printStackTrace(); return; }

        ConnectionManagerObjectRequest incomingRequestsRequest = new ConnectionManagerObjectRequest(EHURLS.BASE + EHURLS.ME_SEGMENT, HTTPMethod.PUT, Optional.of(params.toString()));
        ConnectionManager.sendAsyncRequest(incomingRequestsRequest, new ConnectionManagerCompletionHandler<JSONObject>()
        {
            @Override
            public void onSuccess(JSONObject jsonResponse)
            {
                EnHueco.getInstance().getAppUser().getSchedule().setInstantFreeTimePeriod(Optional.of(newFreeTimePeriod));

                new Handler(Looper.getMainLooper()).post(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        completionListener.onSuccess();
                    }
                });

                LocalBroadcastManager.getInstance(EHApplication.getAppContext()).sendBroadcast(new Intent(CurrentStateManagerNotification.DID_POST_INSTANT_FREE_TIME_PERIOD));
            }

            @Override
            public void onFailure(ConnectionManagerCompoundError error)
            {
                new Handler(Looper.getMainLooper()).post(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        completionListener.onSuccess();
                    }
                });
            }
        });
    }

    public void deleteInstantFreeTimePeriod(final BasicCompletionListener completionListener)
    {
        JSONObject params = new JSONObject();

        try
        {
            params.put("immediate_event", "");
        }
        catch (JSONException e) { e.printStackTrace(); return; }

        ConnectionManagerObjectRequest incomingRequestsRequest = new ConnectionManagerObjectRequest(EHURLS.BASE + EHURLS.ME_SEGMENT, HTTPMethod.PUT, Optional.of(params.toString()));
        ConnectionManager.sendAsyncRequest(incomingRequestsRequest, new ConnectionManagerCompletionHandler<JSONObject>()
        {
            @Override
            public void onSuccess(JSONObject jsonResponse)
            {
                EnHueco.getInstance().getAppUser().getSchedule().setInstantFreeTimePeriod(Optional.<Event>absent());

                new Handler(Looper.getMainLooper()).post(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        completionListener.onSuccess();
                    }
                });

                LocalBroadcastManager.getInstance(EHApplication.getAppContext()).sendBroadcast(new Intent(CurrentStateManagerNotification.DID_POST_INSTANT_FREE_TIME_PERIOD));
            }

            @Override
            public void onFailure(ConnectionManagerCompoundError error)
            {
                new Handler(Looper.getMainLooper()).post(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        completionListener.onSuccess();
                    }
                });
            }
        });
    }
}
