package com.enhueco.model.model;

import com.enhueco.model.other.Utilities;
import com.google.common.base.Optional;
import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

/**
 * Created by Diego on 5/7/16.
 */
public class UserSync
{

    private final String username;
    private final DateTime updatedOn;
    private final DateTime scheduleUpdatedOn;


    public UserSync(JSONObject jsonObject) throws JSONException
    {
        username = jsonObject.getString("login");
        updatedOn = Utilities.getDateTimeFromServerString(jsonObject.getString("updated_on"));
        scheduleUpdatedOn = Utilities.getDateTimeFromServerString(jsonObject.getString("schedule_updated_on"));
    }


    public String getUsername()
    {
        return username;
    }

    public DateTime getUpdatedOn()
    {
        return updatedOn;
    }

    public DateTime getScheduleUpdatedOn()
    {
        return scheduleUpdatedOn;
    }

    public JSONObject toJSON() throws JSONException
    {
        JSONObject object = new JSONObject();
        object.put("login",username);
        return object;
    }
}
