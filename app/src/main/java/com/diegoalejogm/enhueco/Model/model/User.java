package com.diegoalejogm.enhueco.model.model;

import com.diegoalejogm.enhueco.model.logicManagers.ProximityManager;
import com.diegoalejogm.enhueco.model.structures.Tuple;
import com.google.common.base.Optional;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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
     * Represents if user is close to App User. (For efficiency)
     * True if the user is near the App User at the current time, given the currentBSSID values.
     */
    private boolean isNearby;

    /**
     * Current Beacon SSID to which user was connected
     */
    private Optional<String> currentBSSID;

    /**
     * BSSID's Time to live
     */
    private static final int currentBSSIDTimeToLive = 5; //5 Minutes

    /** Last time we notified the app user that this user was nearby */
    private Optional<Date> lastNotifiedNearbyStatusDate;

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
            extractedValues.put("schedule", object.getJSONArray("gap_set"));
            extractedValues.put("containsSchedule", true);
        }
        else
        {
            extractedValues.put("containsSchedule", false);
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
        boolean userIsNotUpdated = false;
        if( isNewUser || (userIsNotUpdated = updatedOn.compareTo(this.getUpdatedOn()) > 0))
        {
            String username = (String) values.get("username");
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

        boolean objectContainsScheduleInformation = (Boolean)values.get("containsSchedule");

        if(objectContainsScheduleInformation)
        {
            boolean scheduleIsNotUpdated = false;

            // Updates schedule only if schedule's last update date in server is newer
            if(isNewUser|| (scheduleIsNotUpdated = ((Date)values.get("scheduleUpdatedOn")).compareTo(this.schedule.getUpdatedOn()) > 0))
            {
                this.schedule = Schedule.fromJSON((Date)values.get("scheduleUpdatedOn"), (JSONArray) values.get("schedule"));
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
            if (event.getType().equals(Event.EventType.FREE_TIME) && event.isCurrentlyHappening())
            {
                return Optional.of(event);
            }
        }

        return Optional.absent();
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
    public Optional<Event> getNextFreeTimePeriod()
    {
        Optional<Event> nextFreeTimePeriod = Optional.absent();

        Calendar localCalendar = Calendar.getInstance();
        int localWeekDayNumber = localCalendar.get(Calendar.DAY_OF_WEEK);

        for (Event event : schedule.getWeekDays()[localWeekDayNumber].getEvents())
        {
            if (event.getType().equals(Event.EventType.FREE_TIME) && event.isAfterCurrentTime())
            {
                return Optional.of(event);
            }
        }

        return nextFreeTimePeriod;
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

    /** Returns user's current and next free time periods. */
    public Tuple<Optional<Event>, Optional<Event>> getCurrentAndNextFreeTimePeriods()
    {
        Optional<Event> currentFreeTime = Optional.absent();

        Calendar localCalendar = Calendar.getInstance();
        int localWeekDayNumber = localCalendar.get(Calendar.DAY_OF_WEEK);

        for (Event event : schedule.getWeekDays()[localWeekDayNumber].getEvents())
        {
            if (event.getType().equals(Event.EventType.FREE_TIME) && event.isCurrentlyHappening())
            {
                currentFreeTime = Optional.of(event);
            }
            else if (event.isAfterCurrentTime())
            {
                return new Tuple<>(currentFreeTime, Optional.of(event));
            }
        }

        return new Tuple<>(currentFreeTime, Optional.<Event>absent());
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

        if (currentBSSID.isPresent())
        {
            Executors.newSingleThreadScheduledExecutor().schedule(new Runnable()
            {
                @Override
                public void run()
                {
                    setCurrentBSSID(Optional.<String>absent());
                }
            }, currentBSSIDTimeToLive, TimeUnit.MINUTES);
        }

        refreshIsNearby();
    }

    public Optional<Date> getLastNotifiedNearbyStatusDate()
    {
        return lastNotifiedNearbyStatusDate;
    }

    public void setLastNotifiedNearbyStatusDate(Optional<Date> lastNotifiedNearbyStatusDate)
    {
        this.lastNotifiedNearbyStatusDate = lastNotifiedNearbyStatusDate;
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
