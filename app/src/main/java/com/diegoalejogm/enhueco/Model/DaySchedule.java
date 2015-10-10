package com.diegoalejogm.enhueco.Model;

import java.util.ArrayList;

/**
 * Created by Diego on 10/9/15.
 */
public class DaySchedule
{
    private String weekDayName;
    private ArrayList<EventEH> gaps;
    private ArrayList<EventEH> classes;

    public DaySchedule(String weekDayName) { this.weekDayName = weekDayName; }

    public ArrayList<EventEH> getGaps()
    {
        return gaps;
    }

    public void setGaps(ArrayList<EventEH> gaps)
    {
        this.gaps = gaps;
    }

    public ArrayList<EventEH> getClasses()
    {
        return classes;
    }

    public void setClasses(ArrayList<EventEH> classes)
    {
        this.classes = classes;
    }
}
