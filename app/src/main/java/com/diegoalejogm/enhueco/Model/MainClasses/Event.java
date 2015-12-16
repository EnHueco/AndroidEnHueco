package com.diegoalejogm.enhueco.Model.MainClasses;

import com.diegoalejogm.enhueco.Model.Other.Utilities;
import com.google.common.base.Optional;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.sql.Time;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by Diego on 10/9/15.
 */

public class Event implements Serializable, Comparable<Event>
{
    @Override
    public int compareTo(Event another)
    {
        if(another == null) return 1;
        Calendar thisLocal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        thisLocal.set(Calendar.HOUR_OF_DAY, this.getStartHour().get(Calendar.HOUR_OF_DAY));
        thisLocal.set(Calendar.MINUTE, this.getStartHour().get(Calendar.MINUTE));
        thisLocal.setTimeZone(TimeZone.getDefault());

        Calendar anotherLocal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        anotherLocal.set(Calendar.HOUR_OF_DAY, another.getStartHour().get(Calendar.HOUR_OF_DAY));
        anotherLocal.set(Calendar.MINUTE, another.getStartHour().get(Calendar.MINUTE));
        anotherLocal.setTimeZone(TimeZone.getDefault());

        // this has a greater time
        if((thisLocal.get(Calendar.HOUR_OF_DAY) > anotherLocal.get(Calendar.HOUR_OF_DAY))
                || ( thisLocal.get(Calendar.HOUR_OF_DAY) == anotherLocal.get(Calendar.HOUR_OF_DAY) &&
                     thisLocal.get(Calendar.MINUTE) > anotherLocal.get(Calendar.MINUTE))
                )
            return 1;
        // both have same time
        else if ( thisLocal.get(Calendar.HOUR_OF_DAY) == anotherLocal.get(Calendar.HOUR_OF_DAY) &&
                thisLocal.get(Calendar.MINUTE) == anotherLocal.get(Calendar.MINUTE))
            return 0;
        // another has greater time
        else return -1;
    }

    public boolean isAfterCurrentTime()
    {
        Event event = new Event(null, Calendar.getInstance(TimeZone.getTimeZone("UTC")),null);
        return this.compareTo(event) > 0;
    }

    public enum EventType
    {
        FREE_TIME, CLASS
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

    public static Event fromJSONObject(JSONObject object) throws JSONException
    {
        // Type, name and location
        String typeString = object.getString("type");
        String name = object.getString("name");
        String location = object.getString("location");
        EventType type = typeString.equals("FREE_TIME") || typeString.equals("GAP")? EventType.FREE_TIME : EventType.CLASS;

        // Weekdays
        int startHourWeekday = Integer.parseInt(object.getString("start_hour_weekday"));
        int endHourWeekday = Integer.parseInt(object.getString("end_hour_weekday"));

        // Start hour
        String[] startHourStringComponents = object.getString("start_hour").split(":");
        int startHour = Integer.parseInt(startHourStringComponents[0]);
        int startMinute = Integer.parseInt(startHourStringComponents[1]);

        String[] endHourStringComponents = object.getString("end_hour").split(":");
        int endHour = Integer.parseInt(endHourStringComponents[0]);
        int endMinute = Integer.parseInt(endHourStringComponents[1]);

        Calendar startHourCalendar = Utilities.calendarWithWeekdayHourMinute(startHourWeekday, startHour, startMinute);
        Calendar endHourCalendar = Utilities.calendarWithWeekdayHourMinute(endHourWeekday, endHour, endMinute);

        return new Event(type, Optional.fromNullable(name), Optional.fromNullable(location), startHourCalendar, endHourCalendar);
    }


    public JSONObject toJSONObject()
    {
        JSONObject object = new JSONObject();
        try
        {
            object.put("type", type);
            object.put("name", name.orNull());
            object.put("location", location.orNull());
            object.put("start_hour_weekday", startHour.get(Calendar.DAY_OF_WEEK));
            object.put("end_hour_weekday", endHour.get(Calendar.DAY_OF_WEEK));
            object.put("start_hour", startHour.get(Calendar.HOUR_OF_DAY)+":"+ startHour.get(Calendar.MINUTE));
            object.put("end_hour", endHour.get(Calendar.HOUR_OF_DAY)+":"+ endHour.get(Calendar.MINUTE));
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        return object;
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
        return (Calendar) startHour.clone();
    }

    public Calendar getEndHour()
    {
        return (Calendar) endHour.clone();
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
        globalCalendar.set(Calendar.HOUR_OF_DAY, startHour.get(Calendar.HOUR_OF_DAY));
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
        globalCalendar.set(Calendar.HOUR_OF_DAY, endHour.get(Calendar.HOUR_OF_DAY));
        globalCalendar.set(Calendar.MINUTE, endHour.get(Calendar.MINUTE));
        globalCalendar.set(Calendar.SECOND, 0);

        return globalCalendar.getTime();
    }

    public Calendar getStartHourCalendarInLocalTimezone()
    {
        Calendar cal = ((Calendar)startHour.clone());
        cal.setTimeZone(TimeZone.getDefault());
        return cal;
    }

    public Calendar getEndHourCalendarInLocalTimezone()
    {
        Calendar cal = ((Calendar)endHour.clone());
        cal.setTimeZone(TimeZone.getDefault());
        return cal;
    }

    public boolean isCurrentlyHappening()
    {
        Calendar current = Calendar.getInstance(TimeZone.getDefault());
        Calendar startHour = this.getStartHourCalendarInLocalTimezone();
        Calendar endHour = this.getEndHourCalendarInLocalTimezone();

        boolean isAfterStart = current.get(Calendar.HOUR_OF_DAY) > startHour.get(Calendar.HOUR_OF_DAY)
                || ( current.get(Calendar.HOUR_OF_DAY) == startHour.get(Calendar.HOUR_OF_DAY) &&
                    current.get(Calendar.MINUTE) >= startHour.get(Calendar.MINUTE)
                    );

        boolean isBeforeEnd = current.get(Calendar.HOUR_OF_DAY) < endHour.get(Calendar.HOUR_OF_DAY)
                || ( current.get(Calendar.HOUR_OF_DAY) == endHour.get(Calendar.HOUR_OF_DAY) &&
                current.get(Calendar.MINUTE) < startHour.get(Calendar.MINUTE)
        );

        return isAfterStart && isBeforeEnd;
    }
}
