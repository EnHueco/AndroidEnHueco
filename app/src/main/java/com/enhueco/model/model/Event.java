package com.enhueco.model.model;

import android.util.Log;
import com.bumptech.glide.util.Util;
import com.enhueco.model.model.immediateEvent.ImmediateEvent;
import com.enhueco.model.model.immediateEvent.InstantFreeTimeEvent;
import com.enhueco.model.other.Utilities;
import com.google.common.base.Optional;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalTime;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by Diego on 10/9/15.
 */

public class Event implements Serializable, Comparable<Event>
{


    private int endHourWeekday;
    private int startHourWeekday;

    public enum EventType
    {
        FREE_TIME, CLASS
    }

    //////////////////////////////////
    //          Attributes          //
    //////////////////////////////////

    /**
     * Day Schedule to which the event is assigned
     */
    private DaySchedule daySchedule;

    /**
     * Name of the event
     */
    private final Optional<String> name;

    /**
     * Type of the event
     */
    private final EventType type;

    /**
     * Event's start hour
     */
    private final LocalTime startHour;

    /**
     * Event's end hour
     */
    private final LocalTime endHour;

    /**
     * Event's location
     */
    private final Optional<String> location;

    //////////////////////////////////
    //    Constructors & Helpers    //
    //////////////////////////////////

    public Event(EventType type, Optional<String> name, Optional<String> location, int startHourWeekday, int
            startHour, int startMinute, int endHourWeekday, int endHour, int endMinute)
    {
        this.type = type;
        this.name = name;
        this.location = location;

        this.startHour = new LocalTime(startHour, startMinute);
        this.startHourWeekday = startHourWeekday;
        this.endHour = new LocalTime(endHour, endMinute);
        this.endHourWeekday = endHourWeekday;
    }

    public Event(ImmediateEvent iEvent)
    {
        this.type = EventType.FREE_TIME;
        this.name = Optional.of(iEvent.getName());
        this.location = Optional.of(iEvent.getLocation());
        this.endHour = iEvent.getEndHour();
        this.startHour = iEvent.getEndHour();
        this.startHourWeekday = DateTime.now(DateTimeZone.UTC).getDayOfWeek();
        this.endHourWeekday = DateTime.now(DateTimeZone.UTC).getDayOfWeek();
    }

    /**
     * Creates new Event from a JSONObject representation
     *
     * @param object JSONObject representation
     * @return Event Event created
     * @throws JSONException JSON if object not successfully created
     */
    public Event(JSONObject object) throws JSONException
    {
        // Type
        String typeString = object.getString("type");

        // Start hour
        String[] startHourStringComponents = object.getString("start_hour").split(":");
        int startHour = Integer.parseInt(startHourStringComponents[0]);
        int startMinute = Integer.parseInt(startHourStringComponents[1]);

        String[] endHourStringComponents = object.getString("end_hour").split(":");
        int endHour = Integer.parseInt(endHourStringComponents[0]);
        int endMinute = Integer.parseInt(endHourStringComponents[1]);

        this.name = Optional.fromNullable(object.getString("name"));
        this.location = Optional.fromNullable(object.getString("location"));
        this.type = typeString.equals("FREE_TIME") ? EventType.FREE_TIME : EventType.CLASS;
        this.startHour = new LocalTime(startHour, startMinute);
        this.endHour = new LocalTime(endHour, endMinute);
        this.startHourWeekday = Utilities.serverWeekDayToJodaWeekDay(Integer.parseInt(object.getString
                ("start_hour_weekday")));
        this.endHourWeekday = Utilities.serverWeekDayToJodaWeekDay(Integer.parseInt(object.getString
                ("end_hour_weekday")));

    }

    //////////////////////////////////
    //      Main Functionality      //
    //////////////////////////////////

    /**
     * Checks if event's time is after current time.
     *
     * @return afterCurrentTime True if event is after current time.
     */
    public boolean isAfterCurrentTime()
    {
        return startHour.isAfter(LocalTime.now(DateTimeZone.UTC));
    }

    /**
     * Generates a JSONObject representation of the event
     *
     * @return object JSONObject representation of event
     */
    public JSONObject toJSONObject() throws JSONException
    {
        JSONObject object = new JSONObject();

        object.put("type", type);
        object.put("name", name.orNull());
        object.put("location", location.orNull());
        object.put("start_hour_weekday", Utilities.jodaWeekDayToServerWeekDay(startHourWeekday));
        object.put("end_hour_weekday", Utilities.jodaWeekDayToServerWeekDay(endHourWeekday));
        object.put("start_hour", startHour.getHourOfDay() + ":" + startHour.getMinuteOfHour());
        object.put("end_hour", endHour.getHourOfDay() + ":" + endHour.getMinuteOfHour());

        return object;
    }

    //TODO
    /*

    /**
     * Returns the start hour (Weekday, Hour, Minute) by setting the components to the date provided.
     *
     * @param date Date to which start hour will be set
     * @return newDate Date with new components

    public Date getStartHourInDate(Date date)
    {
        // TODO
        /*Calendar globalCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        globalCalendar.setTime(date);

        globalCalendar.set(Calendar.DAY_OF_WEEK, startHour.get(Calendar.DAY_OF_WEEK));
        globalCalendar.set(Calendar.HOUR_OF_DAY, startHour.get(Calendar.HOUR_OF_DAY));
        globalCalendar.set(Calendar.MINUTE, startHour.get(Calendar.MINUTE));
        globalCalendar.set(Calendar.SECOND, 0);

        return globalCalendar.getTime();

    }

    /**
     * Returns the end hour (Weekday, Hour, Minute) by setting the components to the date provided.
     *
     * @param date Date to which start hour will be set
     * @return newDate Date with new components

    public Date getEndHourInDate(Date date)
    {

        Calendar globalCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        globalCalendar.setTime(date);

        globalCalendar.set(Calendar.DAY_OF_WEEK, endHour.get(Calendar.DAY_OF_WEEK));
        globalCalendar.set(Calendar.HOUR_OF_DAY, endHour.get(Calendar.HOUR_OF_DAY));
        globalCalendar.set(Calendar.MINUTE, endHour.get(Calendar.MINUTE));
        globalCalendar.set(Calendar.SECOND, 0);

        return globalCalendar.getTime();
    }

    /**
     * Retrieves a calendar in local timezone with event's start hour.
     *
     * @return cal Calendar with event's start hour.

    public Calendar getStartHourCalendarInLocalTimezone()
    {
        Calendar cal = ((Calendar) startHour.clone());
        Log.v("EVENT", cal.getTimeZone().getDisplayName());
        Log.v("EVENT", cal.get(Calendar.HOUR_OF_DAY) + "");
        cal.setTimeZone(TimeZone.getDefault());
        Log.v("EVENT", cal.getTimeZone().getDisplayName());
        Log.v("EVENT", cal.get(Calendar.HOUR_OF_DAY) + "");

        return cal;
    }

    /**
     * Retrieves a calendar in local timezone with event's end hour.
     *
     * @return cal Calendar with event's end hour.

    public Calendar getEndHourCalendarInLocalTimezone()
    {
        Calendar cal = ((Calendar) endHour.clone());
        cal.setTimeZone(TimeZone.getTimeZone("UTC"));
        cal.setTimeZone(TimeZone.getDefault());
        return cal;
    }

    */
    /**
     * Checks if event is currently happening.
     *
     * @return happening True if event is happening right now, false otherwise
     */
    public boolean isCurrentlyHappening()
    {
        LocalTime current = new LocalTime(DateTimeZone.UTC);
        boolean happening = current.isAfter(this.startHour) && current.isBefore(this.endHour);
        return happening;
    }

    //////////////////////////////////
    //      Getters & Setters       //
    //////////////////////////////////

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

    public LocalTime getStartHour()
    {
        return startHour;
    }

    public LocalTime getStartHourInLocalTimezone()
    {
        return startHour.toDateTimeToday(DateTimeZone.UTC).withZone(DateTimeZone.forTimeZone(TimeZone.getDefault()))
                .toLocalTime();
    }

    public LocalTime getEndHourInLocalTimezone()
    {
        return endHour.toDateTimeToday(DateTimeZone.UTC).withZone(DateTimeZone.forTimeZone(TimeZone.getDefault()))
                .toLocalTime();
    }

    public LocalTime getEndHour()
    {
        return endHour;
    }

    public Optional<String> getLocation()
    {
        return location;
    }

    public int getEndHourWeekday()
    {
        return endHourWeekday;
    }

    public int getStartHourWeekday()
    {
        return startHourWeekday;
    }

    public int getLocalWeekDay()
    {
        DateTime dateTime = startHour.toDateTimeToday(DateTimeZone.UTC);
        while(dateTime.getDayOfWeek() != startHourWeekday) dateTime = dateTime.plusDays(1);
        dateTime = dateTime.withZone(DateTimeZone.forTimeZone(TimeZone.getDefault()));
        return dateTime.getDayOfWeek();
    }

    /////////////////////////////////
    //          Comparable         //
    /////////////////////////////////

    @Override
    public int compareTo(Event another)
    {
        return startHour.compareTo(another.startHour);
    }
}
