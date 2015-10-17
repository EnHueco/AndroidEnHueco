package com.diegoalejogm.enhueco.Model.MainClasses;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.Editable;
import com.diegoalejogm.enhueco.Model.Other.Utilities;
import com.google.common.base.Optional;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by Diego on 10/9/15.
 */

public class Event implements Serializable
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


    public Event(EventType type, Optional<String> name, Optional<String> location, Calendar startHour, Calendar endHour)
    {
        this.type = type;
        this.name = name;
        this.startHour = startHour;
        this.endHour = endHour;
        this.location = location;
    }

    public Event(EventType type, Calendar startHour, Calendar endHour)
    {
        this.type = type;
        this.name = Optional.absent();
        this.startHour = startHour;
        this.endHour = endHour;
        this.location = Optional.absent();
    }

    public Event(EventType type, Optional<String> name, Optional<String> location, int startHour, int startMinute, int endHour, int endMinute)
    {
        this.type = type;
        this.name = name;
        this.location = location;

        Calendar startCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        startCalendar.set(Calendar.HOUR_OF_DAY, startHour);
        startCalendar.set(Calendar.MINUTE, startMinute);
        this.startHour = startCalendar;

        Calendar endCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        endCalendar.set(Calendar.HOUR_OF_DAY, endHour);
        endCalendar.set(Calendar.MINUTE, endMinute);
        this.endHour = startCalendar;

    }

    public static Event eventFromJSONObject (JSONObject object) throws JSONException
    {
        String typeString = object.getString("type");
        EventType type = typeString.equals("GAP")? EventType.GAP : EventType.CLASS;

        String name = object.getString("day");
        String location = object.getString("location");

        String[] startHourStringComponents = object.getString("start_hour").split(":");
        int globalStartHourWeekDay = Integer.parseInt(startHourStringComponents[0]);
        int startHour = Integer.parseInt(startHourStringComponents[1]);
        int startMinute = Integer.parseInt(startHourStringComponents[3]);

        String[] endHourStringComponents = object.getString("start_hour").split(":");
        int globalEndHourWeekDay = Integer.parseInt(endHourStringComponents[0]);
        int endHour = Integer.parseInt(endHourStringComponents[1]);
        int endMinute = Integer.parseInt(endHourStringComponents[3]);

        Calendar startHourCalendar = Utilities.calendarWithWeekdayHourMinute(globalStartHourWeekDay, startHour, startMinute);
        Calendar endHourCalendar = Utilities.calendarWithWeekdayHourMinute(globalEndHourWeekDay, endHour, endMinute);

        return new Event(type, Optional.of(name), Optional.of(location), startHourCalendar, endHourCalendar);
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
