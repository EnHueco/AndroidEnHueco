package com.diegoalejogm.enhueco.model.logicManagers;

import android.os.Handler;
import android.os.Looper;
import com.diegoalejogm.enhueco.model.logicManagers.genericManagers.connectionManager.*;
import com.diegoalejogm.enhueco.model.model.AppUser;
import com.diegoalejogm.enhueco.model.model.EnHueco;
import com.diegoalejogm.enhueco.model.other.BasicCompletionListener;
import com.diegoalejogm.enhueco.model.other.EHURLS;
import com.diegoalejogm.enhueco.model.other.JSONResponse;
import com.google.common.base.Optional;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;

/**
 * Created by Diego on 2/28/16.
 */
public class AccountManager
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

            ConnectionManagerRequest request = new ConnectionManagerRequest(EHURLS.BASE + EHURLS.AUTH_SEGMENT, HTTPMethod.POST, Optional.of(params), false);

            ConnectionManager.sendAsyncRequest(request, new ConnectionManagerCompletionHandler()
            {
                @Override
                public void onSuccess(JSONResponse eitherJSONObjectOrJSONArray)
                {
                    try
                    {
                        EnHueco.getInstance().setAppUser(AppUser.userFromJSONObject(eitherJSONObjectOrJSONArray.jsonObject));
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
