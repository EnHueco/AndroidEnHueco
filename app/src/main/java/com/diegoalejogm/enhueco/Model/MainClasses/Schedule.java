package com.diegoalejogm.enhueco.Model.MainClasses;

/**
 * Created by Diego on 10/11/15.
 */
public class Schedule
{
    private final DaySchedule[] weekDays = new DaySchedule[7];

    public Schedule ()
    {
        String[] weekDayNames = {"Domingo", "Lunes", "Martes", "Miercoles", "Jueves", "Viernes", "Sabado"};

        for (int i = 0 ; i < weekDayNames.length ; i++)
        {
            weekDays[i] = new DaySchedule(weekDayNames[i]);
        }
    }

    public DaySchedule[] getWeekDays()
    {
        return weekDays;
    }
}
