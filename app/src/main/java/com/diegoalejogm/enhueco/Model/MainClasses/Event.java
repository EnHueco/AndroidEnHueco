package com.diegoalejogm.enhueco.Model.MainClasses;

import com.google.common.base.Optional;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by Diego on 10/9/15.
 */

public class Event
{
    public enum EventType
    {
        GAP, CLASS
    }
    
    private DaySchedule daySchedule;

    private final Optional<String> name;
    private final EventType type;

    private final Calendar startHour;
    private final Calendar endHour;
    private final Optional<String> location;

    public Event(EventType type, Optional<String> name, Calendar startHour, Calendar endHour, Optional<String> location)
    {
        this.type = type;
        this.name = name;
        this.startHour = startHour;
        this.endHour = endHour;
        this.location = location;
    }

    public DaySchedule getDaySchedule()
    {
        return daySchedule;
    }

    public void setDaySchedule(DaySchedule daySchedule)
    {
        this.daySchedule = daySchedule;
    }

    public Optional<String> getName()
    {
        return name;
    }

    public EventType getType()
    {
        return type;
    }

    public Calendar getStartHour()
    {
        return startHour;
    }

    public Calendar getEndHour()
    {
        return endHour;
    }

    public Optional<String> getLocation()
    {
        return location;
    }

    /** Returns the start hour (Weekday, Hour, Minute) by setting the components to the date provided. */
    public Date getStartHourInDate (Date date)
    {
        Calendar globalCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        globalCalendar.setTime(date);

        globalCalendar.set(Calendar.DAY_OF_WEEK, startHour.get(Calendar.DAY_OF_WEEK));
        globalCalendar.set(Calendar.HOUR, startHour.get(Calendar.HOUR));
        globalCalendar.set(Calendar.MINUTE, startHour.get(Calendar.MINUTE));
        globalCalendar.set(Calendar.SECOND, 0);

        return globalCalendar.getTime();
    }

    /** Returns the end hour (Weekday, Hour, Minute) by setting the components to the date provided. */
    public Date getEndHourInDate (Date date)
    {
        Calendar globalCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        globalCalendar.setTime(date);

        globalCalendar.set(Calendar.DAY_OF_WEEK, endHour.get(Calendar.DAY_OF_WEEK));
        globalCalendar.set(Calendar.HOUR, endHour.get(Calendar.HOUR));
        globalCalendar.set(Calendar.MINUTE, endHour.get(Calendar.MINUTE));
        globalCalendar.set(Calendar.SECOND, 0);

        return globalCalendar.getTime();
    }
}
