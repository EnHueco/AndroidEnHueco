package com.diegoalejogm.enhueco.Model.MainClasses;

import android.net.Uri;
import com.diegoalejogm.enhueco.Model.Other.Tuple;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;

import java.util.*;

/**
 * Created by Diego on 10/11/15.
 */
public class AppUser extends User
{
    private String token;

    private Collection<User> friends = new ArrayList<>();
    private Collection<User> outgoingFriendRequests = new ArrayList<>();
    private Collection<User> incomingFriendRequests = new ArrayList<>();

    public AppUser(String username, String firstNames, String lastNames, String phoneNumber, Optional<Uri> imageURL, String ID, Date lastUpdatedOn)
    {
        super(username, firstNames, lastNames, phoneNumber, imageURL, ID, lastUpdatedOn);
    }

    public Collection<User> getFriends()
    {
        return friends;
    }

    /**
        Returns all friends that are currently in gap.
        @return Friend in gap with their current gap
     */
    public List<Tuple<User, Event>> getFriendsCurrentlyInGap ()
    {
        List<Tuple<User, Event>> friendsAndGaps =  new ArrayList<>();

        for (User friend: friends)
        {
            Optional<Event> currentGap = friend.getCurrentGap();

            if (currentGap.isPresent())
            {
                friendsAndGaps.add(new Tuple<User, Event>(friend, currentGap.get()));
            }
        }

        return  friendsAndGaps;
    }

    public Schedule getCommonGapsScheduleForUsers (User[] users)
    {
        Date currentDate = new Date();
        Schedule commonGapsSchedule = new Schedule();

        if (users.length < 2) return commonGapsSchedule;

        for (int i = 0; i < getSchedule().getWeekDays().length; i++)
        {
            Predicate<Event> eventsFilterPredicate = new Predicate<Event>()
            {
                @Override public boolean apply(Event event) { return event.getType().equals(Event.EventType.GAP); }
            };

            Collection<Event> currentCommonGaps = Collections2.filter(users[0].getSchedule().getWeekDays()[i].getEvents(), eventsFilterPredicate);

            for (int j = 1; j < users.length; j++)
            {
                Collection<Event> newCommonGaps = new ArrayList<>();

                for (Event gap1: currentCommonGaps)
                {
                    Date startHourInCurrentDate1 = gap1.getStartHourInDate(currentDate);
                    Date endHourInCurrentDate1 = gap1.getEndHourInDate(currentDate);

                    for (Event gap2: Collections2.filter(users[j].getSchedule().getWeekDays()[i].getEvents(), eventsFilterPredicate))
                    {
                        Date startHourInCurrentDate2 = gap2.getStartHourInDate(currentDate);
                        Date endHourInCurrentDate2 = gap2.getEndHourInDate(currentDate);

                        if (!(endHourInCurrentDate1.before(startHourInCurrentDate2) || startHourInCurrentDate1.after(endHourInCurrentDate2)))
                        {
                            Calendar startHour = ((startHourInCurrentDate1.after(startHourInCurrentDate2) && startHourInCurrentDate1.before(endHourInCurrentDate2))? gap1.getStartHour() : gap2.getStartHour());
                            Calendar endHour = ((endHourInCurrentDate1.after(startHourInCurrentDate2) && endHourInCurrentDate1.before(endHourInCurrentDate2))? gap1.getEndHour() : gap2.getEndHour());

                            newCommonGaps.add(new Event(Event.EventType.GAP, startHour, endHour));
                        }
                    }
                }

                currentCommonGaps = newCommonGaps;
            }

            commonGapsSchedule.getWeekDays()[i].setEvents(currentCommonGaps);
        }

        return commonGapsSchedule;
    }
}
