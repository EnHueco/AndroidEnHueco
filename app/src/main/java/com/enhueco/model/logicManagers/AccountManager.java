package com.enhueco.model.logicManagers;

import android.os.Handler;
import android.os.Looper;
import com.enhueco.model.logicManagers.genericManagers.connectionManager.*;
import com.enhueco.model.model.AppUser;
import com.enhueco.model.model.EnHueco;
import com.enhueco.model.other.BasicCompletionListener;
import com.enhueco.model.other.EHURLS;
import com.google.common.base.Optional;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;

/**
 * Created by Diego on 2/28/16.
 */
public abstract class AccountManager
{
    /**
     * EnHueco's login method.
     *
     * @param username Username of the user that wishes to log in
     * @param password Password of the user that wishes to log in
     */
    public static void login(String username, String password, final BasicCompletionListener completionListener)
    {
        JSONObject params = new JSONObject();

        try
        {
            params.put("user_id", username);
            params.put("password", password);

            ConnectionManagerObjectRequest request = new ConnectionManagerObjectRequest(EHURLS.BASE + EHURLS.AUTH_SEGMENT, HTTPMethod.POST, Optional.of(params.toString()));

            ConnectionManager.sendAsyncRequest(request, new ConnectionManagerCompletionHandler<JSONObject>()
            {
                @Override
                public void onSuccess(JSONObject response)
                {
                    try
                    {
                        EnHueco.getInstance().setAppUser(AppUser.userFromJSONObject(response));
                        PersistenceManager.persistData();

                        new Handler(Looper.getMainLooper()).post(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                completionListener.onSuccess();
                            }
                        });
                    }
                    catch (JSONException | ParseException e)
                    {
                        e.printStackTrace();
                    }
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
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Logs out current app user
     */
    public static void logout()
    {
        EnHueco.getInstance().setAppUser(null);
        PersistenceManager.deletePersistenceData();
    }
}
