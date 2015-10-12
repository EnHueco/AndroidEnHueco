package com.diegoalejogm.enhueco.Model.MainClasses;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Diego on 10/9/15.
 */
public class DaySchedule
{
    private String weekDayName;
    private ArrayList<Event> events;

    public DaySchedule(String weekDayName) { this.weekDayName = weekDayName; }

    public List<Event> getEvents()
    {
        return Collections.unmodifiableList(events);
    }

    public void setEvents(ArrayList<Event> events)
    {
        this.events = events;
    }
}
