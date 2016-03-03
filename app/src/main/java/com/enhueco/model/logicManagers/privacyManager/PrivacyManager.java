package com.enhueco.model.logicManagers.privacyManager;

import com.enhueco.model.other.BasicCompletionListener;
import com.google.common.base.Optional;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Diego Montoya Sefair on 2/28/16.
 */

public abstract class PrivacyManager
{
    /**
     * Turns a setting off (e.g. If called as "turnPrivacySettingOff(PrivacySetting.ShowEventsNames)", nobody will be able to see the names of the user's events.
     */
    public static void turnOffSetting(PrivacySetting setting, Optional<PrivacyPolicy> privacyPolicy, BasicCompletionListener completionListener)
    {
        JSONObject params = new JSONObject();

        try
        {
            params.put(setting.getServerJSONParameterName(), true);
        }
        catch (JSONException e) { e.printStackTrace(); return; }

        //ConnectionManagerObjectRequest incomingRequestsRequest = new ConnectionManagerObjectRequest(EHURLS.BASE + EHURLS.ME_SEGMENT, HTTPMethod.GET, params, false);
    }

    /**
     * Turns a setting on for a given policy applied to a group of friends. This replaces the previous setting that the user had.
     * @param setting Setting to update
     * @param privacyPolicy Policy applied to the group of friends it accepts for parameter. For example, a method call like
     *                      "turnOnSetting(PrivacySetting.ShowNearby, new PrivacyPolicy.EveryoneExcept(aGroup), completionHandler: ...)" will show to everyone except the members of "aGroup"
     *
     *                      If policy is Optional.absent the setting is applied to everyone
     */
    public static void turnOnSetting(PrivacySetting setting, Optional<PrivacyPolicy> privacyPolicy, BasicCompletionListener completionListener)
    {

    }
}
