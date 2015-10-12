package com.diegoalejogm.enhueco.Model.MainClasses;

import com.google.common.base.Optional;

import java.util.Calendar;

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

    private Optional<String> name;
    private EventType type;

    private Calendar startHour;
    private Calendar endHour;
    private Optional<String> location;

    public Event(EventType type, Optional<String> name, Calendar startHour, Calendar endHour, Optional<String> location)
    {
        this.type = type;
        this.name = name;
        this.startHour = startHour;
        this.endHour = endHour;
        this.location = location;
    }
}
