package com.diegoalejogm.enhueco.Model.MainClasses;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import com.diegoalejogm.enhueco.Model.EHApplication;
import com.diegoalejogm.enhueco.Model.Other.ConnectionManager.*;
import com.diegoalejogm.enhueco.Model.Other.EHURLS;
import com.diegoalejogm.enhueco.Model.Other.Utilities;
import com.google.common.base.Optional;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Diego on 10/11/15.
 */
public class System
{
    public static final System instance = new System();

    public class EHSystemNotification
    {
        public static final String SYSTEM_DID_LOGIN = "SYSTEM_DID_LOGIN", SYSTEM_COULD_NOT_LOGIN_WITH_ERROR = "SYSTEM_COULD_NOT_LOGIN_WITH_ERROR";
        public static final String SYSTEM_DID_RECEIVE_FRIEND_AND_SCHEDULE_UPDATES = "SYSTEM_DID_RECEIVE_FRIEND_AND_SCHEDULE_UPDATES";
        public static final String SYSTEM_DID_RECEIVE_FRIEND_REQUEST_UPDATES = "SYSTEM_DID_RECEIVE_FRIEND_REQUEST_UPDATES";
        public static final String SYSTEM_DID_ADD_FRIEND = "SYSTEM_DID_ADD_FRIEND";
        public static final String SYSTEM_DID_SEND_FRIEND_REQUEST = "SYSTEM_DID_SEND_FRIEND_REQUEST", SYSTEM_DID_FAIL_TO_SEND_FRIEND_REQUEST = "SYSTEM_DID_FAIL_TO_SEND_FRIEND_REQUEST";
    }

    private AppUser appUser;

    public AppUser getAppUser()
    {
        return appUser;
    }
    
    public void createTestAppUser (Context context)
    {
        appUser = new AppUser("pa.perez11", "", "Diego", "Montoya Sefair", "000000000", Optional.of("https://fbcdn-sphotos-a-a.akamaihd.net/hphotos-ak-xap1/t31.0-8/1498135_821566567860780_1633731954_o.jpg"), "pa.perez11", new Date());
        persistData(context);
    }

    public void login (String username, String password)
    {
        JSONObject params = new JSONObject();

        try
        {
            params.put("user_id", username);
            params.put("password", password);

            ConnectionManagerRequest request = new ConnectionManagerRequest(EHURLS.BASE + EHURLS.AUTH_SEGMENT, HTTPMethod.POST, Optional.of(params));

            ConnectionManager.sendAsyncRequest(request, new ConnectionManagerCompletionHandler()
            {
                @Override
                public void onSuccess(JSONObject responseJSON)
                {
                    try
                    {
                        appUser = AppUser.appUserFromJSONObject(responseJSON);

                        LocalBroadcastManager.getInstance(EHApplication.getAppContext()).sendBroadcast(new Intent(EHSystemNotification.SYSTEM_DID_LOGIN));
                    }
                    catch (JSONException | ParseException e)
                    {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(ConnectionManagerCompoundError error)
                {
                    Intent intent = new Intent(EHSystemNotification.SYSTEM_COULD_NOT_LOGIN_WITH_ERROR);
                    intent.putExtra("error", error.error);
                    LocalBroadcastManager.getInstance(EHApplication.getAppContext()).sendBroadcast(intent);
                }
            });
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    public void logout (Context context)
    {
        deletePersistence(context);
    }

    private void deletePersistence(Context context)
    {
        context.deleteFile(AppUser.FILE_NAME);
    }

    public boolean persistData(Context context)
    {
        try
        {
            FileOutputStream fos = context.openFileOutput(AppUser.FILE_NAME, Context.MODE_PRIVATE);
            ObjectOutputStream os = new ObjectOutputStream(fos);
            os.writeObject(appUser);
            os.close();
            fos.close();
            return true;
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return false;
    }

    public boolean loadDataFromPersistence(Context context)
    {
        try
        {
            FileInputStream fis = context.openFileInput(AppUser.FILE_NAME);
            ObjectInputStream is = new ObjectInputStream(fis);
            appUser = (AppUser) is.readObject();
            is.close();
            fis.close();
            return true;
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        catch (ClassNotFoundException e)
        {
            e.printStackTrace();
        }
        return false;
    }
}
