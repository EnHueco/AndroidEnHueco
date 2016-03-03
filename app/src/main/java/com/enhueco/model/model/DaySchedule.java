package com.enhueco.model.model;

import com.google.common.base.Optional;

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

    /**
     * Event tree
     */
    private TreeSet<Event> events = new TreeSet<>();

    //////////////////////////////////
    //    Constructors & Helpers    //
    //////////////////////////////////

    public DaySchedule(String weekDayName)
    {
        this.weekDayName = weekDayName;
        events = new TreeSet<>();
    }


    //////////////////////////////////
    //      Main Functionality      //
    //////////////////////////////////

    /**
     * Checks if its possible to add an event to dayschedule, excluding an event.
     * @param newEvent Event that will be checked.
     * @param eventToExclude Event to exclude while checking.
     * @return canAdd true if event doesn't overlap with any free time period or class, excluding eventToExclude
     */
    public boolean canAddEvent(Event newEvent, Optional<Event> eventToExclude)
    {
        Date currentDate = new Date();

        Date newEventStartHourInCurrentDate = newEvent.getStartHourInDate(currentDate);
        Date newEventEndHourInCurrentDate = newEvent.getEndHourInDate(currentDate);

        for (Event event : events)
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

    /**
     * Adds event if it doesn't overlap with any other event.
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
    public boolean addEvents(Event[] newEvents)
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
     * @param event Event to be removed
     * @return success If event could be successfully removed
     */
    public boolean removeEvent(Event event)
    {
        return events.remove(event);
    }

    public Optional<Event> getEventWithStartHour(Calendar startHour)
    {
        for (Event event : events)
        {
            if (event.getStartHour().get(Calendar.DAY_OF_WEEK) == startHour.get(Calendar.DAY_OF_WEEK) &&
                    event.getStartHour().get(Calendar.HOUR_OF_DAY) == startHour.get(Calendar.HOUR_OF_DAY) &&
                    event.getStartHour().get(Calendar.MINUTE) == startHour.get(Calendar.MINUTE))
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
}
