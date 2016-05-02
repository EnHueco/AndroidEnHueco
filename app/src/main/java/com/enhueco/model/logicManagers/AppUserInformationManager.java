package com.enhueco.model.logicManagers;

import android.content.Intent;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import com.enhueco.model.EHApplication;
import com.enhueco.model.logicManagers.genericManagers.connectionManager.*;
import com.enhueco.model.logicManagers.privacyManager.PrivacyManager;
import com.enhueco.model.logicManagers.privacyManager.PrivacySetting;
import com.enhueco.model.model.*;
import com.enhueco.model.other.BasicCompletionListener;
import com.enhueco.model.other.EHURLS;
import com.enhueco.model.other.Utilities;
import com.google.common.base.Optional;
import org.json.JSONObject;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Diego on 2/28/16.
 */
public class AppUserInformationManager
{
    private static AppUserInformationManager instance;

    public static AppUserInformationManager getSharedManager()
    {
        if (instance == null)
        {
            instance = new AppUserInformationManager();
        }

        return instance;
    }

    private AppUserInformationManager()
    {
    }

    /**
     * Checks for and downloads any updates from the server including
     * Session Status, Friend list, Friends Schedule, User's Info
     * -EHSystemNotification.SYSTEM_DID_RECEIVE_APPUSER_UPDATE in case of success
     */
    public void fetchUpdatesForAppUserAndSchedule()
    {
        try
        {
            ConnectionManagerObjectRequest incomingRequestsRequest = new ConnectionManagerObjectRequest(EHURLS.BASE + EHURLS.ME_SEGMENT, HTTPMethod.GET, Optional.<String>absent());
            ConnectionManager.sendAsyncRequest(incomingRequestsRequest, new ConnectionManagerCompletionHandler<JSONObject>()
            {
                @Override
                public void onSuccess(JSONObject response)
                {
                    try
                    {
                        AppUser appUser = EnHueco.getInstance().getAppUser();
                        appUser.updateWithJSON(response);

                        if(PersistenceManager.getSharedManager().persistData())
                        {
                            Intent intent = new Intent(EnHueco.EHSystemNotification.SYSTEM_DID_RECEIVE_APPUSER_UPDATE);
                            LocalBroadcastManager.getInstance(EHApplication.getAppContext()).sendBroadcast(intent);
                        }
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(ConnectionManagerCompoundError error)
                {
                }
            });
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }


    public void pushPhoneNumber(final String phoneNumber)
    {
        try
        {
            JSONObject params = new JSONObject();

            params.put("phoneNumber", phoneNumber);

            ConnectionManagerObjectRequest request = new ConnectionManagerObjectRequest(EHURLS.BASE + EHURLS.ME_SEGMENT, HTTPMethod.PUT, Optional.of(params.toString()));
            ConnectionManager.sendAsyncRequest(request, new ConnectionManagerCompletionHandler<JSONObject>()
            {
                @Override
                public void onSuccess(JSONObject response)
                {
                    Intent intent = new Intent(EnHueco.EHSystemNotification.SYSTEM_DID_RECEIVE_APPUSER_UPDATE);
                    LocalBroadcastManager.getInstance(EHApplication.getAppContext()).sendBroadcast(intent);
                    EnHueco.getInstance().getAppUser().setPhoneNumber(phoneNumber);
                    PersistenceManager.getSharedManager().persistData();
                }

                @Override
                public void onFailure(ConnectionManagerCompoundError error)
                {
                    String oldNumber = EnHueco.getInstance().getAppUser().getPhoneNumber();
                    PreferenceManager.getDefaultSharedPreferences(EHApplication.getAppContext()).edit().putString(PrivacyManager.phoneNumberKey, oldNumber);
                    error.error.printStackTrace();
                }
            });
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
