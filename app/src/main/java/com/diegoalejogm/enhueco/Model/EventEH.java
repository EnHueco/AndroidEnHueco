package com.diegoalejogm.enhueco.Model;

import java.util.Calendar;

/**
 * Created by Diego on 10/9/15.
 */
public class EventEH
{
    private DaySchedule daySchedule;

    private String name;

    private Calendar startHour;
    private Calendar endHour;
    private String location;

    public EventEH(String name, Calendar startHour, Calendar endHour, String location)
    {
        this.name = name;
        this.startHour = startHour;
        this.endHour = endHour;
        this.location = location;
    }


}
