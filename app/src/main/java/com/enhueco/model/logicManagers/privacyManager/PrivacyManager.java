package com.enhueco.model.logicManagers.privacyManager;

import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import com.enhueco.R;
import com.enhueco.model.EHApplication;
import com.enhueco.model.logicManagers.PersistenceManager;
import com.enhueco.model.logicManagers.genericManagers.connectionManager.*;
import com.enhueco.model.other.BasicCompletionListener;
import com.enhueco.model.other.EHURLS;
import com.enhueco.model.other.Utilities;
import com.google.common.base.Optional;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

/**
 * Created by Diego Montoya Sefair on 2/28/16.
 */

public class PrivacyManager
{
    private static PrivacyManager instance;

    public static PrivacyManager getSharedManager()
    {
        if (instance == null)
        {
            instance = new PrivacyManager();
        }

        return instance;
    }

    /**
     * Turns a setting off (e.g. If called as "turnPrivacySettingOff(PrivacySetting.ShowEventsNames)", nobody will be able to see the names of the user's events.
     */
    public void turnOffSetting(PrivacySetting setting, Optional<PrivacyPolicy> privacyPolicy, final BasicCompletionListener completionListener)
    {
        JSONObject params = new JSONObject();

        try
        {
            params.put(setting.getServerJSONParameterName(), false);
        }
        catch (JSONException e) { e.printStackTrace(); return; }

        ConnectionManagerObjectRequest incomingRequestsRequest = new ConnectionManagerObjectRequest(EHURLS.BASE + EHURLS.ME_SEGMENT, HTTPMethod.PUT, Optional.of(params.toString()));
        ConnectionManager.sendAsyncRequest(incomingRequestsRequest, new ConnectionManagerCompletionHandler<JSONObject>()
        {
            @Override
            public void onSuccess(JSONObject jsonResponse)
            {
                new Handler(Looper.getMainLooper()).post(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        completionListener.onSuccess();
                    }
                });
            }

            @Override
            public void onFailure(final ConnectionManagerCompoundError error)
            {
                new Handler(Looper.getMainLooper()).post(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        completionListener.onFailure(error.error);
                    }
                });
            }
        });
    }

    /**
     * Turns a setting on for a given policy applied to a group of friends. This replaces the previous setting that the user had.
     * @param setting Setting to update
     * @param privacyPolicy Policy applied to the group of friends it accepts for parameter. For example, a method call like
     *                      "turnOnSetting(PrivacySetting.ShowNearby, new PrivacyPolicy.EveryoneExcept(aGroup), completionHandler: ...)" will show to everyone except the members of "aGroup"
     *
     *                      If policy is Optional.absent the setting is applied to everyone
     */
    public void turnOnSetting(PrivacySetting setting, Optional<PrivacyPolicy> privacyPolicy, final BasicCompletionListener completionListener)
    {
        JSONObject params = new JSONObject();

        try
        {
            params.put(setting.getServerJSONParameterName(), true);
        }
        catch (JSONException e) { e.printStackTrace(); return; }

        ConnectionManagerObjectRequest incomingRequestsRequest = new ConnectionManagerObjectRequest(EHURLS.BASE + EHURLS.ME_SEGMENT, HTTPMethod.PUT, Optional.of(params.toString()));
        ConnectionManager.sendAsyncRequest(incomingRequestsRequest, new ConnectionManagerCompletionHandler<JSONObject>()
        {
            @Override
            public void onSuccess(JSONObject jsonResponse)
            {
                new Handler(Looper.getMainLooper()).post(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        completionListener.onSuccess();
                    }
                });
            }

            @Override
            public void onFailure(ConnectionManagerCompoundError error)
            {
                new Handler(Looper.getMainLooper()).post(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        completionListener.onSuccess();
                    }
                });
            }
        });
    }

    public void turnSetting(Boolean on, PrivacySetting setting, Optional<PrivacyPolicy> privacyPolicy, final BasicCompletionListener completionListener)
    {
        if(on) turnOnSetting(setting, privacyPolicy, completionListener);
        else turnOffSetting(setting, privacyPolicy, completionListener);
    }

    public void persistPrivacySettings(HashMap<PrivacySetting, Object> settings)
    {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(EHApplication.getAppContext());
        for ( Map.Entry<PrivacySetting, Object> entry : settings.entrySet() ) {
            PrivacySetting key = entry.getKey();
            Object value = entry.getValue();
            if(key.equals(PrivacySetting.SHOW_EVENT_LOCATIONS))
            {
                preferences.edit().putBoolean(sharesEventsLocationKey, (Boolean) value).commit();
            }
            else if(key.equals(PrivacySetting.SHOW_EVENT_NAMES))
            {
                preferences.edit().putBoolean(sharesEventsNameKey, (Boolean) value).commit();
            }
            else if(key.equals(PrivacySetting.PHONE_NUMBER))
            {
                preferences.edit().putString(phoneNumberKey, (String) value).commit();
            }
        }
    }

    public void persistPhoneNumber(String phoneNumber)
    {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(EHApplication.getAppContext());
        preferences.edit().putString(phoneNumberKey, phoneNumber).commit();
    }

    public static String phoneNumberKey = EHApplication.getAppContext().getResources().getString(R.string.pref_key_phone_number);
    public static String sharesEventsLocationKey = EHApplication.getAppContext().getResources().getString(R.string.pref_key_shares_events_location);
    public static String sharesEventsNameKey = EHApplication.getAppContext().getResources().getString(R.string.pref_key_shares_events_names);
}
