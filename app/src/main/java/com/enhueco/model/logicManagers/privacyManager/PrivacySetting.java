package com.enhueco.model.logicManagers.privacyManager;

/**
 * Created by Diego Montoya Sefair on 3/2/16.
 *
 * Policy applied to the group of friends it accepts for parameter.
 */
public enum PrivacySetting
{
    SHOW_EVENT_NAMES("shares_event_names"), SHOW_EVENT_LOCATIONS("shares_event_locations"), SHOW_USER_IS_NEARBY("shares_user_nearby"), PHONE_NUMBER("phoneNumber");

    private final String serverJSONParameterName;

    PrivacySetting(String serverJSONParameterName)
    {
        this.serverJSONParameterName = serverJSONParameterName;
    }

    public String getServerJSONParameterName()
    {
        return serverJSONParameterName;
    }
}
