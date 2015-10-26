package com.diegoalejogm.enhueco.Model.MainClasses;

import android.net.Uri;
import com.diegoalejogm.enhueco.Model.Other.Utilities;
import com.google.common.base.Optional;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.net.URL;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Diego on 10/9/15.
 */
public class User extends EHSynchronizable implements Serializable
{
    private final String username;
    private final String firstNames;
    private final String lastNames;

    private Optional<String> imageURL;
    private String phoneNumber;

    private Schedule schedule = new Schedule();

    public User(String username, String firstNames, String lastNames, String phoneNumber, Optional<String> imageURL, String ID, Date lastUpdatedOn)
    {
        super(ID, lastUpdatedOn);

        // Initialize User Data
        this.username = username;
        this.firstNames = firstNames;
        this.lastNames = lastNames;
        this.phoneNumber = phoneNumber;
        this.imageURL = imageURL;
    }

    public static User userFromJSONObject (JSONObject object) throws JSONException, ParseException
    {
        String username = object.getString("login");
        String firstNames = object.getString("firstNames");
        String lastNames = object.getString("lastNames");
        String imageURL = object.getString("imageURL");

        // TODO: Correct lastUpdateOn value
        //Date lastUpdatedOn = Utilities.dateFromServerFormattedString(object.getString("lastUpdated_on"));
        Date lastUpdatedOn = new Date();

        return new User(username, firstNames, lastNames, null, Optional.of(imageURL), username, lastUpdatedOn);
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

    public String getFirstNames()
    {
        return firstNames;
    }

    public String getLastNames()
    {
        return lastNames;
    }

    public Optional<String> getImageURL()
    {
        return imageURL;
    }

    public String getPhoneNumber()
    {
        return phoneNumber;
    }

    public Schedule getSchedule()
    {
        return schedule;
    }

    public String getName()
    {
        return firstNames + " " + lastNames;
    }

    public void setImageURL(Optional<String> imageURL)
    {
        this.imageURL = imageURL;
    }

    public void setPhoneNumber(String phoneNumber)
    {
        this.phoneNumber = phoneNumber;
    }

    public void setSchedule(Schedule schedule)
    {
        this.schedule = schedule;
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
