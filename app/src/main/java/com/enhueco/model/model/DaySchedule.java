package com.enhueco.model.model;

import android.util.Log;
import com.google.common.base.Optional;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalTime;

import java.io.Serializable;
import java.util.*;

/**
 * Created by Diego on 10/9/15.
 */
public class DaySchedule implements Serializable
{

    //////////////////////////////////
    //      Getters & Setters       //
    //////////////////////////////////

    /**
     * Name of day schedule's assigned weekday
     */
    private final String weekDayName;

    private final int weekDay;

    /**
     * Event tree
     */
    private TreeSet<Event> events = new TreeSet<>();

    //////////////////////////////////
    //    Constructors & Helpers    //
    //////////////////////////////////

    public DaySchedule(String weekDayName, int weekDay)
    {
        this.weekDayName = weekDayName;
        this.weekDay = weekDay;
        events = new TreeSet<>();
    }
//////////////////////////////////
    //      Main Functionality      //
    //////////////////////////////////

    /**
     * Checks if its possible to add an event to dayschedule, excluding an event.
     *
     * @param newEvent       Event that will be checked.
     * @param eventToExclude Event to exclude while checking.
     * @return canAdd true if event doesn't overlap with any free time period or class, excluding eventToExclude
     */
    public boolean canAddEvent(Event newEvent, Optional<Event> eventToExclude)
    {
        DateTime newEventStartHourDT = newEvent.getStartHour().toDateTimeToday(DateTimeZone.UTC).withZone
                (DateTimeZone.forTimeZone(TimeZone.getDefault()));
        DateTime newEventEndHourDT = newEvent.getEndHour().toDateTimeToday(DateTimeZone.UTC).withZone
                (DateTimeZone.forTimeZone(TimeZone.getDefault()));
        ;

        for (Event event : events)
        {
            if (!eventToExclude.isPresent() || !eventToExclude.get().equals(newEvent))
            {
                DateTime eventStartHourDT = event.getStartHour().toDateTimeToday(DateTimeZone.UTC).withZone
                        (DateTimeZone.forTimeZone(TimeZone.getDefault()));
                DateTime eventEndHourDT = event.getEndHour().toDateTimeToday(DateTimeZone.UTC).withZone
                        (DateTimeZone.forTimeZone(TimeZone.getDefault()));

                if ((newEventStartHourDT.isAfter(eventStartHourDT) && newEventStartHourDT.isBefore(eventStartHourDT))
                        || (newEventEndHourDT.isBefore(eventEndHourDT) && newEventEndHourDT.isAfter(eventStartHourDT)))
                {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Adds event if it doesn't overlap with any other event.
     *
     * @param newEvent Event to be added
     * @return success Event was successfully added
     */
    public boolean addEvent(Event newEvent)
    {
        if (canAddEvent(newEvent, Optional.<Event>absent()))
        {
            newEvent.setDaySchedule(this);
            events.add(newEvent);

            return true;
        }

        return false;
    }

    /**
     * Adds all events if they don't overlap with any other event. The new events *must* not overlap with themselves, otherwise the method will not be
     * able to add them all.
     *
     * @return True if all events could be added correctly
     */
    private boolean addEvents(Event[] newEvents)
    {
        for (Event newEvent : newEvents)
        {
            if (!addEvent(newEvent))
            {
                return false;
            }
        }

        return true;
    }

    /**
     * Removes an event from event's list
     *
     * @param event Event to be removed
     * @return success If event could be successfully removed
     */
    public boolean removeEvent(Event event)
    {
        return events.remove(event);
    }

    public Optional<Event> getEventWithStartHour(LocalTime startHour)
    {
        for (Event event : events)
        {

            if (event.getStartHour().isEqual(startHour))
            {
                return Optional.of(event);
            }
        }
        return Optional.absent();
    }

    //////////////////////////////////
    //      Getters & Setters       //
    //////////////////////////////////

    public String getWeekDayName()
    {
        return weekDayName;
    }

    public Collection<Event> getEvents()
    {
        return events;
    }

    public void setEvents(Collection<Event> events)
    {
        this.events = new TreeSet<>(events);
    }

    public int getWeekDay()
    {
        return weekDay;
    }
}
