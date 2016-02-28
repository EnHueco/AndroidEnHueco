package com.diegoalejogm.enhueco.model.model;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import com.diegoalejogm.enhueco.model.EHApplication;
import com.diegoalejogm.enhueco.model.logicManagers.genericManagers.connectionManager.*;
import com.diegoalejogm.enhueco.model.other.BasicCompletionListener;
import com.diegoalejogm.enhueco.model.other.CompletionListener;
import com.diegoalejogm.enhueco.model.other.EHURLS;
import com.diegoalejogm.enhueco.model.other.JSONResponse;
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
    //////////////////////////////////
    //          Attributes          //
    //////////////////////////////////

    /**
     * Singleton attribute and only object of the class
     */
    private static System instance = new System();

    /**
     * App's app user
     */
    private AppUser appUser;

    //////////////////////////////////
    //    Constructors & Helpers    //
    //////////////////////////////////

    /**
     * System initialization
     */
    public System()
    {
        loadDataFromPersistence();
        //deletePersistenceData(EHApplication.getAppContext());
    }

    /**
     * Singleton getter for class object
     *
     * @return new System instance if first time called or existing one otherwise
     */
    public static System getInstance()
    {
        if (instance == null) instance = new System();

        return instance;
    }

    //////////////////////////////////
    //      Main Functionality      //
    //////////////////////////////////

    /**
     * Tests the creation of a new appUser instance
     */
    public void createTestAppUser()
    {
        appUser = new AppUser("d.montoya10", "", "Diego", "Montoya Sefair", "3176694189", Optional.of("https://fbcdn-sphotos-a-a.akamaihd.net/hphotos-ak-xap1/t31.0-8/1498135_821566567860780_1633731954_o.jpg"), "pa.perez11", new Date());
        User friend1 = new User("da.gomez11", "Diego Alejandro", "Gomez Mosquera", "3144141917", Optional.of("https://fbcdn-sphotos-e-a.akamaihd.net/hphotos-ak-xat1/v/t1.0-9/1377456_10152974578604740_7067096578609392451_n.jpg?oh=89245c25c3ddaa4f7d1341f7788de261&oe=56925447&__gda__=1448954703_30d0fe175a8ab0b665dc074d63a087d6"), "da.gomez11", new Date());
        User friend2 = new User("cl.jimenez12", "Claudia Lucía", "Jiménez Guarín", "", Optional.<String>absent(), "cl.jimenez12", new Date());
        appUser.getFriends().put(friend1.getUsername(), friend1);
        appUser.getFriends().put(appUser.getUsername(), appUser);

        Calendar startHour = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        startHour.add(Calendar.HOUR_OF_DAY, -2);
        Calendar endHour = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        endHour.add(Calendar.HOUR_OF_DAY, 2);
        Calendar localCalendar = Calendar.getInstance();
        friend1.getSchedule().getWeekDays()[localCalendar.get(Calendar.DAY_OF_WEEK)].addEvent(new Event(Event.EventType.FREE_TIME, startHour, endHour));
        appUser.getFriends().put(friend2.getUsername(), friend2);

        persistData();
    }

    /**
     * System's login method.
     *
     * @param username Username of the user that wishes to log in
     * @param password Password of the user that wishes to log in
     */
    public void login(String username, String password, final BasicCompletionListener completionListener)
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
                        persistData();

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
     *
     * @param context Current application context
     */
    public void logout(Context context)
    {
        appUser = null;
        deletePersistenceData(context);
    }

    /**
     * Searches users with keyword id
     *
     * @param id       Keyword that searches for users
     * @param listener Listener of the event
     */
    public void searchUsers(String id, final CompletionListener<List<User>> listener)
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
                    final ArrayList<User> users = new ArrayList<User>();
                    for (int i = 0; i < array.length(); i++)
                    {
                        JSONObject jsonUser = array.getJSONObject(i);
                        users.add(User.fromJSONObject(jsonUser));
                    }

                    new Handler(Looper.getMainLooper()).post(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            listener.onSuccess(users);
                        }
                    });

                }
                catch (Exception e)
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
                        listener.onFailure(error.error);
                    }
                });
            }
        });
    }

//    PERSISTENCE

    /**
     * Persists all app's system data in path
     *
     * @return true if correctly persisted or false otherwise
     */
    public boolean persistData()
    {
        try
        {
            FileOutputStream fos = EHApplication.getAppContext().openFileOutput(AppUser.FILE_NAME, Context.MODE_PRIVATE);
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

    /**
     * Loads data from persistence
     *
     * @return true if data successfuly loaded, false otherwise
     */
    private boolean loadDataFromPersistence()
    {
        try
        {
            FileInputStream fis = EHApplication.getAppContext().openFileInput(AppUser.FILE_NAME);
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

    /**
     * Deletes persistence contents
     *
     * @param context
     */
    private void deletePersistenceData(Context context)
    {
        context.deleteFile(AppUser.FILE_NAME);
    }

    //////////////////////////////////
    //      Getters & Setters       //
    //////////////////////////////////

    /**
     * System's appUser getter
     *
     * @return
     */
    public AppUser getAppUser()
    {
        return appUser;
    }

    //////////////////////////////////
    //         Other Classes        //
    //////////////////////////////////

    /**
     * Class that contains system notification string constants
     */
    public class EHSystemNotification
    {
        public static final String SYSTEM_DID_RECEIVE_APPUSER_UPDATE = "SYSTEM_DID_RECEIVE_APPUSER_UPDATE";

        public static final String SYSTEM_DID_RECEIVE_FRIEND_AND_SCHEDULE_UPDATES = "SYSTEM_DID_RECEIVE_FRIEND_AND_SCHEDULE_UPDATES";
        public static final String SYSTEM_DID_RECEIVE_FRIEND_REQUEST_UPDATES = "SYSTEM_DID_RECEIVE_FRIEND_REQUEST_UPDATES";
    }
}
