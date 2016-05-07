package com.enhueco.model.model.intents;

import com.google.common.base.Optional;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Diego on 5/3/16.
 */
public class UserIntent
{
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

    public JSONObject toJSON() throws JSONException
    {
        JSONObject user = new JSONObject();
        if (username != null) user.put("login", username);
        if (firstNames != null)user.put("firstNames", firstNames);
        if (lastNames != null) user.put("lastNames",lastNames);
        if (imageURL != null) user.put("imageURL",imageURL.or(""));
        if (imageThumbnail != null) user.put("image_thumbnail", phoneNumber);
        if (phoneNumber != null) user.put("phoneNumber", phoneNumber);
        return user;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public void setFirstNames(String firstNames)
    {
        this.firstNames = firstNames;
    }

    public void setLastNames(String lastNames)
    {
        this.lastNames = lastNames;
    }

    public void setImageURL(Optional<String> imageURL)
    {
        this.imageURL = imageURL;
    }

    public void setPhoneNumber(String phoneNumber)
    {
        this.phoneNumber = phoneNumber;
    }
}
