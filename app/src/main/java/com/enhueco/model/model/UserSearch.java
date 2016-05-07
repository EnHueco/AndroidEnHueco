package com.enhueco.model.model;

import android.app.VoiceInteractor;
import com.google.common.base.Optional;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Diego on 5/7/16.
 */
public class UserSearch
{

    private final String username;
    private final String firstNames;
    private final String lastNames;
    private final Optional<String> imageURL;
    private final String imageThumbnail;


    public UserSearch(JSONObject jsonObject) throws JSONException
    {
        username = jsonObject.getString("login");
        firstNames = jsonObject.getString("firstNames");
        lastNames = jsonObject.getString("lastNames");
        imageURL = Optional.of(jsonObject.getString("imageURL"));
        imageThumbnail = jsonObject.getString("image_thumbnail");
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

    public String getImageThumbnail()
    {
        return imageThumbnail;
    }
}
