package com.diegoalejogm.enhueco.Model.MainClasses;

import android.net.Uri;
import com.google.common.base.Optional;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by Diego on 10/9/15.
 */
public class User extends EHSynchronizable
{
    private final String username;
    private final String firstNames;
    private final String lastNames;

    private Optional<Uri> imageURL;
    private String phoneNumber;

    private final Schedule schedule = new Schedule();

    public User(String username, String firstNames, String lastNames, String phoneNumber, Optional<Uri> imageURL, String ID, Date lastUpdatedOn)
    {
        super(ID, lastUpdatedOn);

        // Initialize User Data
        this.username = username;
        this.firstNames = firstNames;
        this.lastNames = lastNames;
        this.phoneNumber = phoneNumber;
        this.imageURL = imageURL;
    }

    @Override
    public String toString()
    {
        return firstNames + " " + lastNames;
    }

    public String getUsername()
    {
        return username;
    }

    public String name()
    {
        return firstNames + " " + lastNames;
    }

    /** Returns user current gap, or nil if user is not in a gap. */
    public Optional<Event> getCurrentGap ()
    {
        Date currentDate = new Date();

        Calendar localCalendar = Calendar.getInstance();
        int localWeekDayNumber = localCalendar.get(Calendar.DAY_OF_WEEK);

        for (Event event : schedule.getWeekDays()[localWeekDayNumber].getEvents())
        {
            if (event.getType().equals(Event.EventType.GAP))
            {
                Date startHourInCurrentDate = event.getStartHourInDate(currentDate);
                Date endHourInCurrentDate = event.getEndHourInDate(currentDate);

                if(currentDate.after(startHourInCurrentDate) && currentDate.before(endHourInCurrentDate))
                {
                    return Optional.of(event);
                }
            }
        }

        return Optional.absent();
    }

    public Optional<Event> getNextEvent ()
    {
        // TODO
        return null;
    }
}
