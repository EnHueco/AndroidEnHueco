package com.diegoalejogm.enhueco.Model.MainClasses;

import android.net.Uri;
import com.diegoalejogm.enhueco.Model.Other.Utilities;
import com.google.common.base.Optional;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by Diego on 10/9/15.
 */
public class User extends EHSynchronizable implements Serializable
{
    private String username;
    private String firstNames;
    private String lastNames;

    private Optional<String> imageURL;
    private String phoneNumber;

    private Schedule schedule = new Schedule();

//    public class SyncFields

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
        String phoneNumber = object.getString("phoneNumber");
        String lastUpdatedOnString = object.getString("updated_on");

        Date lastUpdatedOn = EHSynchronizable.dateFromServerString(lastUpdatedOnString);

        return new User(username, firstNames, lastNames, phoneNumber, Optional.of(imageURL), username, lastUpdatedOn);
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

    public void updateWithJSON(JSONObject object)
    {
        try
        {
            String updatedOnString = object.getString("updated_on");
            Date updatedOn = EHSynchronizable.dateFromServerString(updatedOnString);

            if(updatedOn.compareTo(this.getUpdatedOn()) > 0)
            {
                this.updateBasicInfoWithJSON(object);
            }

            String scheduleUpdatedOnString = object.getString("schedule_updated_on");
            Date scheduleUpdatedOn = EHSynchronizable.dateFromServerString(scheduleUpdatedOnString);

            if(scheduleUpdatedOn.compareTo(this.schedule.getUpdatedOn()) > 0)
            {
                this.schedule = Schedule.fromJSONArray(scheduleUpdatedOn, object.getJSONArray("gap_set"));
            }

        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    private void updateBasicInfoWithJSON(JSONObject object) throws JSONException
    {
        String username = object.getString("login");
        String firstNames = object.getString("firstNames");
        String lastNames = object.getString("lastNames");
        String imageURL = object.getString("imageURL");
        String phoneNumber = object.getString("phoneNumber");
        String updatedOnString = object.getString("updated_on");
        Date updatedOn = EHSynchronizable.dateFromServerString(updatedOnString);

        this.username = username;
        this.firstNames = firstNames;
        this.lastNames = lastNames;
        this.imageURL = Optional.of(imageURL);
        this.phoneNumber = phoneNumber;
        this.setLastUpdatedOn(updatedOn);
        this.setID(username);
    }


    public static User userWithScheduleFromJSONObject(JSONObject object) throws JSONException
    {
        User user = new User();
        user.updateBasicInfoWithJSON(object);

        String scheduleUpdatedOnString = object.getString("schedule_updated_on");
        Date scheduleUpdatedOn = EHSynchronizable.dateFromServerString(scheduleUpdatedOnString);

        Schedule.fromJSONArray(scheduleUpdatedOn, object.getJSONArray("gap_set"));

        return user;
    }

    /*
        Returns user with values set to null
     */
    private User()
    {
        super(null, null);
    }


}
