package com.diegoalejogm.enhueco.model.model;

import com.google.common.base.Optional;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

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

    public static AppUser userFromJSONObject(JSONObject object) throws JSONException, ParseException
    {
        User user = User.fromJSONObject(object.getJSONObject("user"));
        String token = object.getString("value");
        return new AppUser(user.getUsername(), token, user.getFirstNames(), user.getLastNames(), user.getPhoneNumber(), user.getImageURL(), user.getID(), user.getUpdatedOn());
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
