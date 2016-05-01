package com.enhueco.model.model;

import com.enhueco.model.model.immediateEvent.ImmediateEvent;
import com.google.common.base.Optional;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.*;

/**
 * Created by Diego on 10/11/15.
 */
public class Schedule extends EHSynchronizable implements Serializable
{
    //////////////////////////////////
    //          Attributes          //
    //////////////////////////////////

    /**
     * Array of day schedules for each weekday
     */
    private DaySchedule[] weekDays;

    /**
     * Timer responsible for setting instantFreeTimePeriod to Optional.absent() when necessary.
     */
    //private Timer instantFreeTimePeriodDestroyTimer;


    /**
     * Array of indexed weekday names
     */
    public static final String[] weekDayNames = {"Domingo", "Lunes", "Martes", "Miercoles", "Jueves", "Viernes", "Sabado"};

    //////////////////////////////////
    //    Constructors & Helpers    //
    //////////////////////////////////

    public Schedule()
    {
        super("", new Date());

        weekDays = new DaySchedule[8];

        for (int i = 1; i < weekDays.length; i++)
        {
            weekDays[i] = new DaySchedule(weekDayNames[i - 1]);
        }
    }

    public Schedule(Date updatedOn)
    {
        super("", updatedOn);

        weekDays = new DaySchedule[8];

        for (int i = 1; i < weekDays.length; i++)
        {
            weekDays[i] = new DaySchedule(weekDayNames[i - 1]);
        }
    }


    /**
     * Creates a new schedule from a JSONArray representation
     * @param updatedOn date when schedule was last updated on
     * @param eventsArray array of events added to the newly created schedule
     * @return schedule new schedule with all events and attributes set
     * @throws JSONException if JSONArray is incorrectly typed
     */
    public static Schedule fromJSON(Date updatedOn, JSONArray eventsArray) throws JSONException
    {
        Schedule schedule = new Schedule(updatedOn);

        for(int i = 0; i < eventsArray.length(); i++)
        {
            // Create event
            JSONObject object = eventsArray.getJSONObject(i);
            Event event = Event.fromJSONObject(object);

            //Locate event in local array of weekdays based on its UTC startHour
            Calendar calendar = (Calendar) event.getStartHour().clone();
            calendar.setTimeZone(TimeZone.getDefault());
            int weekDay = calendar.get(Calendar.DAY_OF_WEEK);

            // Add event
            schedule.weekDays[weekDay].addEvent(event);
        }
        return schedule;
    }

    //////////////////////////////////
    //      Getters & Setters       //
    //////////////////////////////////

    public DaySchedule[] getWeekDays()
    {
        return weekDays;
    }

}
