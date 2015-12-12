package com.diegoalejogm.enhueco.Model.MainClasses;

import com.diegoalejogm.enhueco.Model.Managers.ProximityManager;
import com.google.common.base.Optional;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

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

    private boolean isNearby;
    private Optional<String> currentBSSID;

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

    public static User fromJSONObject(JSONObject object) throws JSONException, ParseException
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

    public boolean isNearby()
    {
        return isNearby;
    }

    public Optional<String> getCurrentBSSID()
    {
        return currentBSSID;
    }

    public void setCurrentBSSID(Optional<String> currentBSSID)
    {
        this.currentBSSID = currentBSSID;
        refreshIsNearby();
    }

    public void refreshIsNearby ()
    {
        AppUser appUser = System.getInstance().getAppUser();
        isNearby = currentBSSID.isPresent() && appUser.getCurrentBSSID().isPresent() && ProximityManager.getSharedManager().accessPointsAreNear(currentBSSID.get(), appUser.getCurrentBSSID().get());
    }

    /** Returns user current free time period, or nil if user is not free. */
    public Optional<Event> getCurrentFreeTimePeriod()
    {
        Date currentDate = new Date();

        Calendar localCalendar = Calendar.getInstance();
        int localWeekDayNumber = localCalendar.get(Calendar.DAY_OF_WEEK);

        for (Event event : schedule.getWeekDays()[localWeekDayNumber].getEvents())
        {
            if (event.getType().equals(Event.EventType.FREE_TIME))
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
                this.schedule = Schedule.fromJSON(scheduleUpdatedOn, object.getJSONArray("gap_set"));
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


    public static User fromJSONObjectWithSchedule(JSONObject object) throws JSONException, ParseException
    {
        User user = User.fromJSONObject(object);

        String scheduleUpdatedOnString = object.getString("schedule_updated_on");
        Date scheduleUpdatedOn = EHSynchronizable.dateFromServerString(scheduleUpdatedOnString);

        Schedule.fromJSON(scheduleUpdatedOn, object.getJSONArray("gap_set"));

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
