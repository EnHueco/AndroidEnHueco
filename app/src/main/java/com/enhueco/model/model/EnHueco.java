package com.enhueco.model.model;

import com.enhueco.model.logicManagers.PersistenceManager;
import com.google.common.base.Optional;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by Diego on 10/11/15.
 */
public class EnHueco
{
    //////////////////////////////////
    //          Attributes          //
    //////////////////////////////////

    /**
     * Singleton attribute and only object of the class
     */
    private static EnHueco instance;

    /**
     * App's app user
     */
    private AppUser appUser;

    //////////////////////////////////
    //    Constructors & Helpers    //
    //////////////////////////////////

    /**
     * EnHueco initialization
     */
    public EnHueco()
    {
    }

    /**
     * Singleton getter for class object
     *
     * @return new EnHueco instance if first time called or existing one otherwise
     */
    public static EnHueco getInstance()
    {
        if (instance == null)
        {
            instance = new EnHueco();
            PersistenceManager.loadDataFromPersistence();
        }

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

        PersistenceManager.persistData();
    }

    //////////////////////////////////
    //      Getters & Setters       //
    //////////////////////////////////

    /**
     * EnHueco's appUser getter
     *
     * @return
     */
    public AppUser getAppUser()
    {
        return appUser;
    }

    /**
     * WARNING. Use with caution
     */
    public void setAppUser(AppUser appUser)
    {
        this.appUser = appUser;
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
