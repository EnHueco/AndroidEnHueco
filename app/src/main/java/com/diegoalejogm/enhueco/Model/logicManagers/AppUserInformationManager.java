package com.diegoalejogm.enhueco.model.logicManagers;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import com.diegoalejogm.enhueco.model.EHApplication;
import com.diegoalejogm.enhueco.model.logicManagers.genericManagers.connectionManager.*;
import com.diegoalejogm.enhueco.model.model.*;
import com.diegoalejogm.enhueco.model.other.EHURLS;
import com.diegoalejogm.enhueco.model.other.JSONResponse;
import com.google.common.base.Optional;
import org.json.JSONObject;

import java.util.Date;

/**
 * Created by Diego on 2/28/16.
 */
public class AppUserInformationManager
{
    /**
     * Checks for and downloads any updates from the server including
     * Session Status, Friend list, Friends Schedule, User's Info
     * -EHSystemNotification.SYSTEM_DID_RECEIVE_APPUSER_UPDATE in case of success
     */
    public static void fetchUpdatesForAppUserAndSchedule()
    {
        try
        {
            ConnectionManagerRequest incomingRequestsRequest = new ConnectionManagerRequest(EHURLS.BASE + EHURLS.ME_SEGMENT, HTTPMethod.GET, Optional.<JSONObject>absent(), false);
            ConnectionManager.sendAsyncRequest(incomingRequestsRequest, new ConnectionManagerCompletionHandler()
            {
                @Override
                public void onSuccess(JSONResponse responseJSON)
                {
                    try
                    {
                        AppUser appUser = EnHueco.getInstance().getAppUser();

                        JSONObject responseObject = responseJSON.jsonObject;

                        User user = User.fromJSONObject(responseObject);
                        appUser.setImageURL(user.getImageURL());
                        appUser.setPhoneNumber(user.getPhoneNumber());

                        String scheduleUpdatedOnString = responseObject.getString("schedule_updated_on");
                        Date scheduleUpdatedOn = EHSynchronizable.dateFromServerString(scheduleUpdatedOnString);

                        Schedule schedule = Schedule.fromJSON(scheduleUpdatedOn, responseObject.getJSONArray("gap_set"));
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
