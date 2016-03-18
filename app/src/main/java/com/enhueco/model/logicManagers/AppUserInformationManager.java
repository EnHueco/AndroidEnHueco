package com.enhueco.model.logicManagers;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import com.enhueco.model.EHApplication;
import com.enhueco.model.logicManagers.genericManagers.connectionManager.*;
import com.enhueco.model.model.*;
import com.enhueco.model.other.EHURLS;
import com.enhueco.model.other.Utilities;
import com.google.common.base.Optional;
import org.json.JSONObject;

import java.util.Date;

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

    private AppUserInformationManager () {}

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

                        User user = User.fromJSONObject(response);
                        appUser.setImageURL(user.getImageURL());
                        appUser.setPhoneNumber(user.getPhoneNumber());

                        String scheduleUpdatedOnString = response.getString("schedule_updated_on");
                        Date scheduleUpdatedOn = Utilities.getDateFromServerString(scheduleUpdatedOnString);

                        Schedule schedule = Schedule.fromJSON(scheduleUpdatedOn, response.getJSONArray("gap_set"));
                        appUser.setSchedule(schedule);

                        Intent intent = new Intent(EnHueco.EHSystemNotification.SYSTEM_DID_RECEIVE_APPUSER_UPDATE);
                        LocalBroadcastManager.getInstance(EHApplication.getAppContext()).sendBroadcast(intent);
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
}
