package com.enhueco.model.logicManagers.CurrentStateManager;

import com.enhueco.model.logicManagers.ProximityUpdatesManager;
import com.enhueco.model.model.EnHueco;
import com.enhueco.model.model.Event;
import com.enhueco.model.model.User;
import com.enhueco.model.model.immediateEvent.ImmediateEvent;
import com.enhueco.model.structures.Tuple;
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
            Optional<ImmediateEvent> immediateEvent = friend.getImmediateEvent();
            if(friend.isInvisible())
            {
                continue;
            }

            Optional<Event> currentFreeTimePeriod = friend.getCurrentFreeTimePeriod();

            if(friend.isInInstantFreeTime())
            {
                friendsAndFreeTimePeriods.add(new Tuple<>(friend, new Event(friend.getImmediateEvent().get())));
            }
            else if (currentFreeTimePeriod.isPresent())
            {
                friendsAndFreeTimePeriods.add(new Tuple<>(friend, currentFreeTimePeriod.get()));
            }
        }

        return friendsAndFreeTimePeriods;
    }
}
