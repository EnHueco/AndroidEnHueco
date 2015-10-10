package com.diegoalejogm.enhueco.Model;

import android.net.Uri;

import java.util.Hashtable;

/**
 * Created by Diego on 10/9/15.
 */
public class User
{
    private String username;
    private String firstNames;
    private String lastNames;

    private Uri imageURL;
    private String phoneNumber;

    private DaySchedule[] schedule;


    public User(String username, String firstNames, String lastNames, String phoneNumber, Uri imageURL)
    {
        // Initialize User Data
        this.username = username;
        this.firstNames = firstNames;
        this.lastNames = lastNames;
        this.phoneNumber = phoneNumber;
        this.imageURL = imageURL;

        // Initialize Schedule
        schedule = new DaySchedule[7];
        String[] weekDaysNames = {"Domingo", "Lunes", "Martes", "Miercoles", "Jueves", "Viernes", "Sabado"};
        for (int i = 0 ; i < weekDaysNames.length ; i++)
        {
            schedule[i] = new DaySchedule(weekDaysNames[i]);
        }
    }

//    public String getName() {return firstNames + " " + lastNames;}

    @Override
    public String toString()
    {
        return firstNames + " " + lastNames;
    }

    public String getUsername()
    {
        return username;
    }
}
