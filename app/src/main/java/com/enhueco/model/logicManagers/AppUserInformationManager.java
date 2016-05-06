package com.enhueco.model.logicManagers;

import com.enhueco.model.logicManagers.genericManagers.connectionManager.*;
import com.enhueco.model.model.*;
import com.enhueco.model.model.intents.AppUserIntent;
import com.enhueco.model.other.BasicCompletionListener;
import com.enhueco.model.other.EHURLS;
import com.google.common.base.Optional;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Diego on 2/28/16.
 */
public class AppUserInformationManager extends LogicManager
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
    public void fetchUpdatesForAppUserAndSchedule(final BasicCompletionListener completionListener)
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

                        if (PersistenceManager.getSharedManager().persistData())
                        {
                            callCompletionListenerSuccessHandlerOnMainThread(completionListener);
                        }
                    }
                    catch (Exception e)
                    {
                        callCompletionListenerFailureHandlerOnMainThread(completionListener, e);
                    }
                }

                @Override
                public void onFailure(ConnectionManagerCompoundError error)
                {
                    callCompletionListenerFailureHandlerOnMainThread(completionListener, error.error);
                }
            });
        }
        catch (Exception e)
        {
            callCompletionListenerFailureHandlerOnMainThread(completionListener, e);
        }
    }

    public void updateAppUser(final AppUserIntent userIntent, final BasicCompletionListener completionListener)
    {
        try
        {
            JSONObject params = userIntent.toJSON();

            ConnectionManagerObjectRequest request = new ConnectionManagerObjectRequest(EHURLS.BASE + EHURLS.ME_SEGMENT, HTTPMethod.PUT, Optional.of(params.toString()));
            ConnectionManager.sendAsyncRequest(request, new ConnectionManagerCompletionHandler<JSONObject>()
            {
                @Override
                public void onSuccess(JSONObject response) throws JSONException
                {
                    EnHueco.getInstance().getAppUser().updateWithJSON(response);
                    PersistenceManager.getSharedManager().persistData();

                    callCompletionListenerSuccessHandlerOnMainThread(completionListener);
                }

                @Override
                public void onFailure(ConnectionManagerCompoundError error)
                {
                    callCompletionListenerFailureHandlerOnMainThread(completionListener, error.error);
                }
            });
        }
        catch (Exception e)
        {
            callCompletionListenerFailureHandlerOnMainThread(completionListener, e);
        }
    }
}
