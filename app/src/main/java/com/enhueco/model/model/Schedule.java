package com.enhueco.model.model;

import com.enhueco.model.other.Utilities;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

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

    /**
     * Creates a new schedule from a JSONArray representation
     * @param updatedOn date when schedule was last updated on
     * @param eventsArray array of events added to the newly created schedule
     * @return schedule new schedule with all events and attributes set
     * @throws JSONException if JSONArray is incorrectly typed
     */
    public Schedule(DateTime updatedOn, JSONArray eventsArray) throws JSONException
    {
        super("", updatedOn);

        weekDays = new DaySchedule[8];

        for (int i = 1; i < weekDays.length; i++)
        {
            weekDays[i] = new DaySchedule(weekDayNames[i - 1], i);
        }

        for(int i = 0; i < eventsArray.length(); i++)
        {
            // Create event
            JSONObject object = eventsArray.getJSONObject(i);
            Event event = new Event(object);

            //Locate event in local array of weekdays based on its UTC startHour
            int weekDay = event.getLocalTimezoneWeekDay();

            // TODO : This doesn't work with DST. So stop using array of weekdays.
            // Add event
            weekDays[Utilities.jodaWeekDayToServerWeekDay(weekDay)].addEvent(event);
        }
    }

    //////////////////////////////////
    //      Getters & Setters       //
    //////////////////////////////////

    public DaySchedule[] getWeekDays()
    {
        return weekDays;
    }

}
