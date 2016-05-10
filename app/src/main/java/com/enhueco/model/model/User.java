package com.enhueco.model.model;

import android.app.VoiceInteractor;
import com.bumptech.glide.util.Util;
import com.enhueco.model.logicManagers.ProximityUpdatesManager;
import com.enhueco.model.model.immediateEvent.ImmediateEvent;
import com.enhueco.model.other.Utilities;
import com.enhueco.model.structures.Tuple;
import com.google.common.base.Optional;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
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
     * User's profile image thumbnail
     */
    private String imageThumbnail;

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
     * User visibility state
     */
    private boolean invisible = false;

    /**
     * Current Beacon SSID to which user was connected
     */
    private Optional<String> currentBSSID;

    /**
     * BSSID's Time to live
     */
    private static final int currentBSSIDTimeToLive = 5; //5 Minutes

    /**
     * Last time we notified the app user that this user was nearby
     */
    private Optional<Date> lastNotifiedNearbyStatusDate;

    /**
     * Current's day immediate event . Self-destroys when the period is over (i.e. currentTime > endHour)
     */
    private Optional<ImmediateEvent> instantFreeTimePeriod = Optional.absent();


    /**
     * User's schedule
     */
    private Schedule schedule;


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

    public User(String username, String firstNames, String lastNames, String phoneNumber, Optional<String> imageURL,
                String imageThumbnail, String ID, DateTime updatedOn)
    {
        super(ID, updatedOn);
        this.username = username;
        this.firstNames = firstNames;
        this.lastNames = lastNames;
        this.phoneNumber = phoneNumber;
        this.imageURL = imageURL;
        this.imageThumbnail = imageThumbnail;
        this.setID(ID);
    }

    /**
     * Generates a new User object from JSONObject representation
     *
     * @param object JSONObject representation to be decoded
     * @return User Newly generated User
     * @throws JSONException Thrown if JSON encoded object was encoded wrong
     */
    public User(JSONObject object) throws JSONException
    {
        super(object.getString("login"), Utilities.getDateTimeFromServerString((String) object.get("updated_on")));

        username = object.getString("login");
        firstNames = object.getString("firstNames");
        lastNames = object.getString("lastNames");
        imageURL = Optional.of(object.getString("imageURL"));
        imageThumbnail = object.getString("image_thumbnail");
        phoneNumber = object.getString("phoneNumber");

        schedule = new Schedule(Utilities.getDateTimeFromServerString(object.getString("schedule_updated_on")),
                (JSONArray) object.get("gap_set"));
        instantFreeTimePeriod = (Optional.of(new ImmediateEvent(object.getJSONObject("immediate_event"))));

    }


    /**
     * Updates user with JSONObject representation
     *
     * @param object JSONObject representation of new values to add to user
     */
    public void updateWithJSON(JSONObject object) throws JSONException
    {
        String updatedOnString = (String) object.get("updated_on");
        DateTime updatedOn = Utilities.getDateTimeFromServerString(updatedOnString);

        this.username = object.getString("login");
        this.firstNames = object.getString("firstNames");
        this.lastNames = object.getString("lastNames");
        this.imageURL = Optional.of(object.getString("imageURL"));
        this.imageThumbnail = object.getString("image_thumbnail");
        this.phoneNumber = object.getString("phoneNumber");

        this.setID(username);
        this.setUpdatedOn(updatedOn);

        if (object.has("gap_set"))
        {
            this.schedule = new Schedule(Utilities.getDateTimeFromServerString(object.getString
                    ("schedule_updated_on")), object.getJSONArray("gap_set"));
        }

        if (object.has("immediate_event"))
        {
            instantFreeTimePeriod = (Optional.of(new ImmediateEvent(object.getJSONObject("immediate_event"))));
        }
    }


    //////////////////////////////////
    //      Main Functionality      //
    //////////////////////////////////


    /**
     * Updates user's isNearby state.
     */
    public void refreshIsNearby()
    {
        AppUser appUser = EnHueco.getInstance().getAppUser();
        isNearby = currentBSSID.isPresent() && appUser.getCurrentBSSID().isPresent() && ProximityUpdatesManager.getSharedManager().accessPointsAreNear(currentBSSID.get(), appUser.getCurrentBSSID().get());
    }

    /**
     * Returns user current free time period
     *
     * @return currentFreeTimePeriod Current free time period or nil, if user is not free
     */
    public Optional<Event> getCurrentFreeTimePeriod()
    {
        int weekDay = Utilities.jodaWeekDayToServerWeekDay(DateTime.now().getDayOfWeek());

        for (Event event : this.getSchedule().getWeekDays()[weekDay].getEvents())
        {
            if (!event.getType().equals(Event.EventType.FREE_TIME)) continue;
            if (event.isCurrentlyHappening())
            {
                return Optional.of(event);
            }
        }
        return Optional.absent();
    }

    /**
     * Retrives user's next event
     *
     * @return event Next event in calendar after current one or current time
     */
    public Optional<Event> getNextEvent()
    {
        // TODO
        return null;
    }

    /**
     * Retrieves User's next "FreeTime" Event
     *
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
     * Returns user's current and next free time periods.
     */
    public Tuple<Optional<Event>, Optional<Event>> getCurrentAndNextFreeTimePeriods()
    {
        Optional<Event> currentFreeTime = Optional.absent();

        int localWeekDayNumber = Utilities.jodaWeekDayToServerWeekDay(DateTime.now().getDayOfWeek());

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

    public String getImageThumbnail()
    {
        return imageThumbnail;
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

    public Optional<ImmediateEvent> getInstantFreeTimePeriod()
    {
        return instantFreeTimePeriod;
    }

    public void setInstantFreeTimePeriod(Optional<ImmediateEvent> instantFreeTimePeriod)
    {
        this.instantFreeTimePeriod = instantFreeTimePeriod;
/*
        if (instantFreeTimePeriod.isPresent())
        {
            if (instantFreeTimePeriodDestroyTimer != null)
            {
                instantFreeTimePeriodDestroyTimer.cancel();
            }

            instantFreeTimePeriodDestroyTimer = new Timer();

            TimerTask deleteEvent = new TimerTask () {
                @Override
                public void run () {
                    setInstantFreeTimePeriod(Optional.<ImmediateEvent>absent());
                }
            };

            instantFreeTimePeriodDestroyTimer.schedule(deleteEvent, instantFreeTimePeriod.get().getEndHourInDate(new Date()));
        }
        */
    }

}
