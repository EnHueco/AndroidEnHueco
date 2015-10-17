package com.diegoalejogm.enhueco.Model.MainClasses;

import java.io.Serializable;

/**
 * Created by Diego on 10/11/15.
 */
public class Schedule implements Serializable
{
    private final DaySchedule[] weekDays;

    public static final String[] weekDayNames = {"Domingo", "Lunes", "Martes", "Miercoles", "Jueves", "Viernes", "Sabado"};

    public Schedule()
    {
        weekDays = new DaySchedule[8];


        for (int i = 1; i < weekDays.length; i++)
        {
            weekDays[i] = new DaySchedule(weekDayNames[i - 1]);
        }
    }

    public DaySchedule[] getWeekDays()
    {
        return weekDays;
    }
}
