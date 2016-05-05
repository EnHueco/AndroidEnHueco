package com.enhueco.model.model.intents;

import com.enhueco.model.logicManagers.privacyManager.PrivacyManager;
import com.enhueco.model.logicManagers.privacyManager.PrivacySetting;
import com.enhueco.model.model.Schedule;
import com.enhueco.model.model.immediateEvent.ImmediateEvent;
import com.google.common.base.Optional;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

/**
 * Created by Diego on 5/3/16.
 */
public class AppUserIntent extends UserIntent
{
    public JSONObject toJSON() throws JSONException
    {
        JSONObject user = super.toJSON();
        user.put(PrivacySetting.SHOW_EVENT_LOCATIONS.getServerJSONParameterName(), PrivacyManager.getSharedManager().getSharesEventsLocation());
        user.put(PrivacySetting.SHOW_EVENT_NAMES.getServerJSONParameterName(), PrivacyManager.getSharedManager().getSharesEventsNames());
        return user;
    }

}
