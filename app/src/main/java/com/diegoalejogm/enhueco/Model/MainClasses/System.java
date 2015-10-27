package com.diegoalejogm.enhueco.Model.MainClasses;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import com.diegoalejogm.enhueco.Model.EHApplication;
import com.diegoalejogm.enhueco.Model.Other.ConnectionManager.*;
import com.diegoalejogm.enhueco.Model.Other.EHURLS;
import com.diegoalejogm.enhueco.Model.Other.JSONResponse;
import com.diegoalejogm.enhueco.View.SearchNewFriendsActivity;
import com.google.common.base.Optional;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.text.ParseException;
import java.util.*;

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

        public static final String SYSTEM_RECEIVED_USER_SEARCH_RESPONSE = "SYSTEM_RECEIVED_USER_SEARCH_RESPONSE" ;
        public static final String SYSTEM_DID_RECEIVE_FRIEND_REQUEST_ACCEPT = "SYSTEM_DID_RECEIVE_FRIEND_REQUEST_ACCEPT";
        public static final String SYSTEM_DID_RECEIVE_FRIEND_DELETION = "SYSTEM_DID_RECEIVE_FRIEND_DELETION";
    }

    private AppUser appUser;

    public System ()
    {
        //deletePersistence(EHApplication.getAppContext());
    }

    public AppUser getAppUser()
    {
        return appUser;
    }
    
    public void createTestAppUser (Context context)
    {
        appUser = new AppUser("d.montoya10", "", "Diego", "Montoya Sefair", "3176694189", Optional.of("https://fbcdn-sphotos-a-a.akamaihd.net/hphotos-ak-xap1/t31.0-8/1498135_821566567860780_1633731954_o.jpg"), "pa.perez11", new Date());
        User friend1 = new User("da.gomez11","Diego Alejandro", "Gomez Mosquera", "3144141917", Optional.of("https://fbcdn-sphotos-e-a.akamaihd.net/hphotos-ak-xat1/v/t1.0-9/1377456_10152974578604740_7067096578609392451_n.jpg?oh=89245c25c3ddaa4f7d1341f7788de261&oe=56925447&__gda__=1448954703_30d0fe175a8ab0b665dc074d63a087d6"), "da.gomez11", new Date());
        User friend2 = new User("cl.jimenez12","Claudia Lucía", "Jiménez Guarín", "", Optional.<String>absent(), "cl.jimenez12", new Date());
        appUser.getFriends().add(friend1);
        appUser.getFriends().add(appUser);

        Calendar startHour = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        startHour.add(Calendar.HOUR_OF_DAY, -2);
        Calendar endHour = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        endHour.add(Calendar.HOUR_OF_DAY, 2);
        Calendar localCalendar = Calendar.getInstance();
        friend1.getSchedule().getWeekDays()[localCalendar.get(Calendar.DAY_OF_WEEK)].addEvent(new Event(Event.EventType.GAP, startHour, endHour));
        appUser.getFriends().add(friend2);

        persistData(context);
    }

    public void login(String username, String password, final Context applicationContext)
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
                        appUser = AppUser.userFromJSONObject(eitherJSONObjectOrJSONArray.jsonObject);
                        // TODO: Persist User
                        persistData(applicationContext);
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
        appUser = null;
        deletePersistence(context);
    }

    private void deletePersistence(Context context)
    {
        context.deleteFile(AppUser.FILE_NAME);
    }


    public void searchUsers(String id)
    {
        ConnectionManagerRequest request = new ConnectionManagerRequest(EHURLS.BASE + EHURLS.USERS_SEARCH + id, HTTPMethod.GET, Optional.<JSONObject>absent(), true);

        ConnectionManager.sendAsyncRequest(request, new ConnectionManagerCompletionHandler()
        {

            @Override
            public void onSuccess(JSONResponse eitherJSONObjectOrJSONArray)
            {
                try
                {
                    JSONArray array = eitherJSONObjectOrJSONArray.jsonArray;
                    ArrayList<User> users = new ArrayList<User>();
                    for (int i = 0; i < array.length(); i++)
                    {
                        JSONObject jsonUser = array.getJSONObject(i);
                        users.add(User.fromJSONObject(jsonUser));
                    }

                    Intent intent = new Intent(EHSystemNotification.SYSTEM_RECEIVED_USER_SEARCH_RESPONSE);
                    intent.putExtra(SearchNewFriendsActivity.EXTRA_USERS, users);
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
                Intent intent = new Intent(EHSystemNotification.SYSTEM_COULD_NOT_LOGIN_WITH_ERROR);
                intent.putExtra("error", error.error);
                LocalBroadcastManager.getInstance(EHApplication.getAppContext()).sendBroadcast(intent);
            }
        });
    }

//    ---- PERSISTENCE ----

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
        catch (ClassNotFoundException | IOException e)
        {
            e.printStackTrace();
        }
        return false;
    }
}
