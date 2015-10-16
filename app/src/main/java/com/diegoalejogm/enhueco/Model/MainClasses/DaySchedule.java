package com.diegoalejogm.enhueco.Model.MainClasses;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;

import java.util.*;

/**
 * Created by Diego on 10/9/15.
 */
public class DaySchedule
{
    private final String weekDayName;
    private ArrayList<Event> events = new ArrayList<>();

    public DaySchedule(String weekDayName) { this.weekDayName = weekDayName; }

    public Collection<Event> getEvents()
    {
        return Collections.unmodifiableList(events);
    }

    public void setEvents(Collection<Event> events)
    {
        this.events = Lists.newArrayList(events);
    }

    /** Returns true if event doesn't overlap with any gap or class, excluding eventToExclude. */
    public boolean canAddEvent (Event newEvent, Optional<Event> eventToExclude)
    {
        Date currentDate = new Date();

        Date newEventStartHourInCurrentDate = newEvent.getStartHourInDate(currentDate);
        Date newEventEndHourInCurrentDate = newEvent.getEndHourInDate(currentDate);

        for (Event event: events)
        {
            if (!eventToExclude.isPresent() || !eventToExclude.get().equals(newEvent))
            {
                Date startHourInCurrentDate = event.getStartHourInDate(currentDate);
                Date endHourInCurrentDate = event.getEndHourInDate(currentDate);

                if (!(newEventEndHourInCurrentDate.before(startHourInCurrentDate) || newEventStartHourInCurrentDate.after(endHourInCurrentDate)))
                {
                    return false;
                }
            }
        }

        return true;
    }

    /** Adds event if it doesn't overlap with any other event */
    public boolean addEvent (Event newEvent)
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
        Adds all events if they don't overlap with any other event. The new events *must* not overlap with themselves, otherwise the method will not be
        able to add them all.

        @return True if all events could be added correctly
     */
    public boolean addEvents(Event[] newEvents)
    {
        for (Event newEvent : newEvents)
        {
            if (!addEvent(newEvent)) { return false; }
        }

        return true;
    }

    public boolean removeEvent (Event event)
    {
        return events.remove(event);
    }

    public Optional<Event> eventWithStartHour (Calendar startHour)
    {
        for (Event event: events)
        {
            if (event.getStartHour().get(Calendar.DAY_OF_WEEK) == startHour.get(Calendar.DAY_OF_WEEK) &&
                    event.getStartHour().get(Calendar.HOUR) == startHour.get(Calendar.HOUR) &&
                    event.getStartHour().get(Calendar.MINUTE) == startHour.get(Calendar.MINUTE))
            {
                return Optional.of(event);
            }
        }
        return Optional.absent();
    }
}
