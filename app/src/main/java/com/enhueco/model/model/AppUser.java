package com.enhueco.model.model;

import android.util.Log;
import com.enhueco.model.logicManagers.privacyManager.PrivacyManager;
import com.enhueco.model.logicManagers.privacyManager.PrivacySetting;
import com.google.common.base.Optional;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

public class AppUser extends User implements Serializable
{
    public class UserStringEncodingSeparationCharacters
    {
        // Values for QR encoding
        static final char splitCharacter = '\\';
        public static final char separationCharacter = '-';
        public static final char multipleElementsCharacter = ',';
        public static final char hourMinuteSeparationCharacter = ':';
    }

    //////////////////////////////////
    //          Attributes          //
    //////////////////////////////////

    /**
     * Log String for this class
     */
    private static final String LOG = "AppUser";

    /**
     * App User Token to acces API services
     */
    private String token;

    /**
     * Dictionary of app user's friends
     */
    private HashMap<String, User> friends = new HashMap<>();

    /**
     * Values for persistence
     */
    public static final String FILE_NAME = "appUser";

    //////////////////////////////////
    //    Constructors & Helpers    //
    //////////////////////////////////

    public AppUser(String username, String token, String firstNames, String lastNames, String phoneNumber, Optional<String> imageURL, String ID, Date lastUpdatedOn)
    {
        super(username, firstNames, lastNames, phoneNumber, imageURL, ID, lastUpdatedOn);

        this.token = token;
    }

    public AppUser(JSONObject object) throws JSONException, ParseException
    {
        // Create User
        super(object.getJSONObject("user"));
        token = object.getString("value");

        JSONObject userJSON = object.getJSONObject("user");

        // Persist privacy settings
        HashMap<PrivacySetting, Object> settings = new HashMap<>();
        settings.put(PrivacySetting.SHOW_EVENT_LOCATIONS, userJSON.getBoolean(PrivacySetting.SHOW_EVENT_LOCATIONS.getServerJSONParameterName()));
        settings.put(PrivacySetting.SHOW_EVENT_NAMES, userJSON.getBoolean(PrivacySetting.SHOW_EVENT_NAMES.getServerJSONParameterName()));
        settings.put(PrivacySetting.PHONE_NUMBER, userJSON.getString(PrivacySetting.PHONE_NUMBER.getServerJSONParameterName()));
        PrivacyManager.getSharedManager().persistPrivacySettings(settings);
    }


    public void updateWithJSON(JSONObject object)  throws JSONException
    {
        super.updateWithJSON(object);

        HashMap<PrivacySetting, Object> settings = new HashMap<>();
        settings.put(PrivacySetting.SHOW_EVENT_LOCATIONS, object.getBoolean(PrivacySetting.SHOW_EVENT_LOCATIONS.getServerJSONParameterName()));
        settings.put(PrivacySetting.SHOW_EVENT_NAMES, object.getBoolean(PrivacySetting.SHOW_EVENT_NAMES.getServerJSONParameterName()));
        settings.put(PrivacySetting.PHONE_NUMBER, object.getString(PrivacySetting.PHONE_NUMBER.getServerJSONParameterName()));
        PrivacyManager.getSharedManager().persistPrivacySettings(settings);

    }
    //////////////////////////////////
    //      Main Functionality      //
    //////////////////////////////////

    @Override
    public void refreshIsNearby()
    {
        if (getCurrentBSSID().isPresent())
        {
            for (User friend : friends.values())
            {
                friend.refreshIsNearby();
            }
        }
    }

    /**
     * Generates QR encoded representation of user.
     *
     * @return representation QR encoded representation of user.
     */
    public String getStringEncodedRepresentation()
    {
        StringBuilder sb = new StringBuilder();

        // Add username
        sb.append(getUsername());
        sb.append(UserStringEncodingSeparationCharacters.splitCharacter);
        // Add names
        sb.append(getFirstNames());
        sb.append(UserStringEncodingSeparationCharacters.separationCharacter);
        sb.append(getLastNames());
        sb.append(UserStringEncodingSeparationCharacters.splitCharacter);
        // Add phone
        sb.append(getPhoneNumber());
        sb.append(UserStringEncodingSeparationCharacters.splitCharacter);
        // Add image
        sb.append(getImageURL().get());
        sb.append(UserStringEncodingSeparationCharacters.splitCharacter);

        boolean firstEvent = true;

        // Add events
        int i = 1;

        for (DaySchedule currentDS : getSchedule().getWeekDays())
        {
            for (Event currentEvent : currentDS.getEvents())
            {
                Event.EventType eventType = currentEvent.getType();
                DecimalFormat mFormat = new DecimalFormat("00");

                if (firstEvent) firstEvent = false;
                else if (!firstEvent) sb.append(UserStringEncodingSeparationCharacters.multipleElementsCharacter);
                // Add Class and weekday
                sb.append(eventType.equals(Event.EventType.CLASS) ? 'C' : 'G');
                sb.append(UserStringEncodingSeparationCharacters.separationCharacter);
                sb.append(i);
                sb.append(UserStringEncodingSeparationCharacters.separationCharacter);
                // Add hours
                sb.append(mFormat.format(currentEvent.getStartHour().get(Calendar.HOUR_OF_DAY)));
                sb.append(UserStringEncodingSeparationCharacters.hourMinuteSeparationCharacter);
                sb.append(mFormat.format(currentEvent.getStartHour().get(Calendar.MINUTE)));
                sb.append(UserStringEncodingSeparationCharacters.separationCharacter);
                sb.append(mFormat.format(currentEvent.getEndHour().get(Calendar.HOUR_OF_DAY)));
                sb.append(UserStringEncodingSeparationCharacters.hourMinuteSeparationCharacter);
                sb.append(mFormat.format(currentEvent.getEndHour().get(Calendar.MINUTE)));
            }
            i++;
        }
        sb.append(UserStringEncodingSeparationCharacters.splitCharacter);
        return sb.toString();
    }

    //////////////////////////////////
    //      Getters & Setters       //
    //////////////////////////////////

    public String getToken()
    {
        return token;
    }

    public HashMap<String, User> getFriends()
    {
        return friends; //new ArrayList<User>(Arrays.asList(this));
    }
}
