package com.diegoalejogm.enhueco.Model.MainClasses;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by Diego on 10/11/15.
 */
public class Schedule extends EHSynchronizable implements Serializable
{
    private DaySchedule[] weekDays;

    private Date updatedOn;

    public static final String[] weekDayNames = {"Domingo", "Lunes", "Martes", "Miercoles", "Jueves", "Viernes", "Sabado"};

    public Schedule()
    {
        super("", new Date());

        weekDays = new DaySchedule[8];
        updatedOn = new Date();

        for (int i = 1; i < weekDays.length; i++)
        {
            weekDays[i] = new DaySchedule(weekDayNames[i - 1]);
        }
    }

    public Schedule(Date updatedOn)
    {
        super("", updatedOn);

        weekDays = new DaySchedule[8];
        updatedOn = new Date();

        for (int i = 1; i < weekDays.length; i++)
        {
            weekDays[i] = new DaySchedule(weekDayNames[i - 1]);
        }
    }

    public DaySchedule[] getWeekDays()
    {
        return weekDays;
    }

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
}
