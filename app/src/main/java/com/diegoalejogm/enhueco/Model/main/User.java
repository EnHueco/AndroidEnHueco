package com.diegoalejogm.enhueco.model.main;

import com.diegoalejogm.enhueco.model.managers.ProximityManager;
import com.google.common.base.Optional;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

/**
 * Created by Diego on 10/9/15.
 */
public class User extends EHSynchronizable implements Serializable
{
    //////////////////////////////
    //        Attributes        //
    //////////////////////////////

    /**
     * User's unique username
     */
    private String username;

    /**
     * User's first names
     */
    private String firstNames;

    /**
     * User's last names
     */
    private String lastNames;

    /**
     * User's profile image URL
     */
    private Optional<String> imageURL;

    /**
     * User's phone number
     */
    private String phoneNumber;

    /**
     * Represents if user is close to App User. True if it is, False otherwise
     */
    private boolean isNearby;

    /**
     * Current Beacon SSID to which user was connected
     */
    private Optional<String> currentBSSID;

    /**
     * User's schedule
     */
    private Schedule schedule = new Schedule();


    ///////////////////////////////////////
    //      Constructors & Helpers       //
    ///////////////////////////////////////

    /**
     * Returns user with values set to null
     */
    private User()
    {
        super(null, null);
    }

    public User(String username, String firstNames, String lastNames, String phoneNumber, Optional<String> imageURL, String ID, Date updatedOn)
    {
        super(ID,updatedOn);
        this.username = username;
        this.firstNames = firstNames;
        this.lastNames = lastNames;
        this.phoneNumber = phoneNumber;
        this.imageURL = imageURL;
        this.setID(ID);
    }

    /**
     * Generates a new User object from JSONObject representation
     * @param object JSONObject representation to be decoded
     * @return User Newly generated User
     * @throws JSONException Thrown if JSON encoded object was encoded wrong
     */
    public static User fromJSONObject(JSONObject object) throws JSONException
    {
        User newUser = new User();
        newUser.updateWithJSON(object);
        return newUser;
    }

    /**
     * Extracts JSONObject user values into a dictionary. Useful to have only one point of access to JSON Object data
     * @param object JSONObject representation of User data
     * @return extractedValues Dictionary with User data
     * @throws JSONException Thrown if cannot decode User correctly
     */
    private static HashMap<String, Object> extractJSONObjectUserValues(JSONObject object) throws JSONException
    {
        HashMap<String, Object> extractedValues = new HashMap<>();

        extractedValues.put("username", object.getString("login"));
        extractedValues.put("firstNames", object.getString("firstNames"));
        extractedValues.put("lastNames", object.getString("lastNames"));
        extractedValues.put("imageURL", object.getString("imageURL"));
        extractedValues.put("phoneNumber", object.getString("phoneNumber"));
        extractedValues.put("updatedOn", object.getString("updated_on"));

        if (object.has("schedule_updated_on") && object.has("gap_set"))
        {
            extractedValues.put("scheduleUpdatedOn", EHSynchronizable.dateFromServerString(object.getString("schedule_updated_on")));
            extractedValues.put("gapSet", object.getJSONArray("gap_set"));
        }

        return extractedValues;
    }

    /**
     * Updates user with JSONObject representation
     * @param object JSONObject representation of new values to add to user
     */
    public void updateWithJSON(JSONObject object) throws JSONException
    {

        boolean isNewUser = this.getUpdatedOn() == null || this.schedule == null;
        HashMap<String, Object> values = extractJSONObjectUserValues(object);

        String updatedOnString = (String) values.get("updatedOn");
        Date updatedOn = EHSynchronizable.dateFromServerString(updatedOnString);

        // if JSONObject updatedOn date is newer
        boolean userIsNotUpdated = updatedOn.compareTo(this.getUpdatedOn()) > 0;
        if( isNewUser || userIsNotUpdated)
        {
            String username = (String) values.get("login");
            String firstNames = (String) values.get("firstNames");
            String lastNames = (String) values.get("lastNames");
            String imageURL = (String) values.get("imageURL");
            String phoneNumber = (String) values.get("phoneNumber");

            this.username = username;
            this.firstNames = firstNames;
            this.lastNames = lastNames;
            this.imageURL = Optional.of(imageURL);
            this.phoneNumber = phoneNumber;
            this.setID(username);
            this.setUpdatedOn(updatedOn);
        }

        boolean objectContainsScheduleInformation = values.containsKey("schedule") && values.containsKey("gapSet");

        if(objectContainsScheduleInformation)
        {
            String scheduleUpdatedOnString = object.getString("schedule_updated_on");
            Date scheduleUpdatedOn = EHSynchronizable.dateFromServerString(scheduleUpdatedOnString);


            boolean scheduleIsNotUpdated = scheduleUpdatedOn.compareTo(this.schedule.getUpdatedOn()) > 0;

            // Updates schedule only if schedule's last update date in server is newer
            if(isNewUser|| scheduleIsNotUpdated)
            {
                this.schedule = Schedule.fromJSON((Date)values.get("scheduleUpdatedOn"), (JSONArray) values.get("gapSet"));
            }
        }
    }

    //////////////////////////////////
    //      Main Functionality      //
    //////////////////////////////////


    /**
     * Updates user's isNearby state.
     */
    public void refreshIsNearby ()
    {
        AppUser appUser = System.getInstance().getAppUser();
        isNearby = currentBSSID.isPresent() && appUser.getCurrentBSSID().isPresent() && ProximityManager.getSharedManager().accessPointsAreNear(currentBSSID.get(), appUser.getCurrentBSSID().get());
    }

    /**
     * Returns user current free time period
     * @return currentFreeTimePeriod Current free time period or nil, if user is not free
     */
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

        Optional<Event> currentFreeTimePeriod = Optional.absent();
        return currentFreeTimePeriod;
    }

    /**
     * Retrives user's next event
     * @return event Next event in calendar after current one or current time
     */
    public Optional<Event> getNextEvent ()
    {
        // TODO
        return null;
    }

    /**
     * Retrieves User's next "FreeTime" Event
     * @return ans Next event if exists in same day, null otherwise
     */
    public Event nextFreeTimePeriod()
    {
        Event ans = null;
        Calendar calendar= Calendar.getInstance(TimeZone.getDefault());
        int day = calendar.get(Calendar.DAY_OF_WEEK);

        for(Event event : this.getSchedule().getWeekDays()[day].getEvents())
        {
            if(!event.getType().equals(Event.EventType.FREE_TIME)) continue;
            boolean isAfterCurrentTime = event.isAfterCurrentTime();
            if(isAfterCurrentTime && ( ans == null || event.compareTo(ans) < 0))
            {
                ans = event;
            }
        }
        return ans;
    }

    /**
     * Retrieves User's current free time period if found
     * @return ans Current "Free Time" period if one is going on, otherwise null
     */
    public Event currentFreeTimePeriod()
    {
        Event ans = null;
        Calendar calendar= Calendar.getInstance(TimeZone.getDefault());
        int day = calendar.get(Calendar.DAY_OF_WEEK);

        for(Event event : this.getSchedule().getWeekDays()[day].getEvents())
        {
            if(!event.getType().equals(Event.EventType.FREE_TIME)) continue;
            if(event.isCurrentlyHappening())
            {
                ans = event;
                break;
            }
        }

        return ans;

    }

    /////////////////////////////
    //   Getters and Setters   //
    /////////////////////////////

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

    //////////////////////////////
    //        To String         //
    //////////////////////////////

    @Override
    public String toString()
    {
        return firstNames + " " + lastNames;
    }

}
