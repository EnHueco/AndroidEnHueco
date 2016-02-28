package com.diegoalejogm.enhueco.model.logicManagers;

import android.os.Handler;
import android.os.Looper;
import com.diegoalejogm.enhueco.model.model.EnHueco;
import com.diegoalejogm.enhueco.model.model.Event;
import com.diegoalejogm.enhueco.model.model.User;
import com.diegoalejogm.enhueco.model.other.BasicCompletionListener;
import com.diegoalejogm.enhueco.model.structures.Tuple;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * Created by Diego on 2/28/16.
 */
public class UserStateManager
{
    /**
     * Returns friends who are currently nearby and for who the app user has not been notified for
     * a time longer than ProximityUpdatesManager.MINIMUM_TIME_INTERVAL_BETWEEN_NOTIFICATIONS
     */
    public static Collection<User> getFriendsCurrentlyNearbyAndEligibleForNotification()
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
    public static List<Tuple<User, Event>> getCurrentlyAvailableFriends()
    {
        List<Tuple<User, Event>> friendsAndFreeTimePeriods = new ArrayList<>();

        for (User friend : EnHueco.getInstance().getAppUser().getFriends().values())
        {
            Optional<Event> currentFreeTimePeriod = friend.getCurrentFreeTimePeriod();

            if (currentFreeTimePeriod.isPresent())
            {
                friendsAndFreeTimePeriods.add(new Tuple<User, Event>(friend, currentFreeTimePeriod.get()));
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
    public static void postInstantFreeTimePeriod(Event newFreeTimePeriod, final BasicCompletionListener listener)
    {
        //TODO :

        new Handler(Looper.getMainLooper()).post(new Runnable()
        {
            @Override
            public void run()
            {
                listener.onSuccess();
            }
        });
    }
}
