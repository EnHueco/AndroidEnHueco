package com.enhueco.model.model;

import android.util.Log;
import com.enhueco.model.logicManagers.privacyManager.PrivacyManager;
import com.enhueco.model.logicManagers.privacyManager.PrivacySetting;
import com.google.common.base.Optional;
import org.joda.time.DateTime;
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

    public AppUser(String username, String token, String firstNames, String lastNames, String phoneNumber,
                   Optional<String> imageURL, String imageThumbnail, String ID, DateTime lastUpdatedOn)
    {
        super(username, firstNames, lastNames, phoneNumber, imageURL, imageThumbnail, ID, lastUpdatedOn);

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
