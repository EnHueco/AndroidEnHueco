package com.enhueco.model.logicManagers.privacyManager;

/**
 * Created by Diego Montoya Sefair on 3/2/16.
 *
 * Policy applied to the group of friends it accepts for parameter.
 */
public enum PrivacySetting
{
    SHOW_EVENT_NAMES("show_event_names"), SHOW_EVENT_LOCATIONS("show_event_location"), SHOW_USER_IS_NEARBY("show_user_is_nearby");

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
